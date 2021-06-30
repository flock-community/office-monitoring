package flock.community.office.monitoring.backend.alerting.service.evaluators

import flock.community.office.monitoring.backend.alerting.domain.Alert
import flock.community.office.monitoring.backend.alerting.domain.AlertConfig
import flock.community.office.monitoring.backend.alerting.domain.AlertId
import flock.community.office.monitoring.backend.alerting.domain.HourlyRainForecast
import flock.community.office.monitoring.backend.alerting.domain.Rule
import flock.community.office.monitoring.backend.alerting.domain.RuleState
import flock.community.office.monitoring.backend.alerting.domain.SentAlert
import flock.community.office.monitoring.backend.alerting.domain.toAlertId
import flock.community.office.monitoring.backend.alerting.service.AlertSenderService
import flock.community.office.monitoring.backend.alerting.service.RuleStateService
import flock.community.office.monitoring.backend.alerting.service.TimedUpdatesEventBus
import flock.community.office.monitoring.backend.alerting.service.TimedUpdateRequest
import flock.community.office.monitoring.backend.device.configuration.toDeviceName
import flock.community.office.monitoring.utils.logging.loggerFor
import kotlinx.coroutines.flow.Flow
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

data class AlertCheckEvaluatorData(
    val ruleState: RuleState,
    val rule: Rule
)

@Component
class AlertCheckEvaluator(
    private val timedUpdatesEventBus: TimedUpdatesEventBus,
    private val alertSenderService: AlertSenderService,
    private val ruleStateService: RuleStateService,
    @Qualifier("alerts") private val alerts: Map<AlertId, Alert>,
) : AlertingEvaluator<TimedUpdateRequest, AlertCheckEvaluatorData> {

    private val log = loggerFor<AlertCheckEvaluator>()

    override fun subscribeToUpdates(rule: Rule): Flow<TimedUpdateRequest> {
        return timedUpdatesEventBus.subscribe(rule)
    }

    override suspend fun handleUpdate(
        update: TimedUpdateRequest,
        previousStateValue: AlertCheckEvaluatorData
    ): AlertCheckEvaluatorData {
        log.info("Resolving timedUpdate because: ${update.triggerReason}")

        val x: List<SentAlert> = listOfNotNull(
            // FIXME: Quick hack to report on changes in open/closed contact sensors
            trySendContactSensorChangeAlert(previousStateValue.ruleState, previousStateValue.rule),
            trySendWeatherUpdateAlert(previousStateValue.ruleState, previousStateValue.rule)
        )

        if (x.isNotEmpty()) {
            return AlertCheckEvaluatorData(
                previousStateValue.ruleState.copy(
                    sentAlerts = previousStateValue.ruleState.sentAlerts + x,
                    lastStateChange = Instant.now()
                ), previousStateValue.rule
            )
        }

        return previousStateValue

    }


    private suspend fun trySendContactSensorChangeAlert(
        ruleState: RuleState,
        rule: Rule
    ): SentAlert? {
        if (ruleState.openedContactSensors != ruleState.sentAlerts.maxByOrNull { it.dateTime }?.openedContactSensors) {
            // send message
            val alert = alerts.entries.find {
                it.value.alertId.value.contains("every-contact-change") && it.value.alertId.value.contains(rule.id.value)
            }?.value

            alert?.let {
                val properties = getAlertProperties(ruleState, rule)
                alertSenderService.send(it, properties)


                return SentAlert(
                    openedContactSensors = ruleState.openedContactSensors,
                    alertId = it.alertId, // TODO make me pretty
                    dateTime = Instant.now()
                )

            }
        }
        return null
    }

    private suspend fun trySendWeatherUpdateAlert(ruleState: RuleState, rule: Rule): SentAlert? {
        // TODO: Determine if fixed time alerting is necessary (e.g. every day at 5pm? latest state)
        // Out of scope?
        val sentAlerts = rule.alerts.mapNotNull {
            // Sent message at configured time
            val time = it.value.time
            if (time != null) {
                val currentTime = LocalTime.from(Instant.now().atZone(ZoneId.of("Europe/Paris")))
                if (Duration.between(currentTime, time).abs().seconds < 30) {
                    log.info("Within time window to send Alert ${it.key}.")
                    val properties = getAlertProperties(ruleState, rule)
                    alertSenderService.send(it.value, properties)

                    SentAlert(ruleState.openedContactSensors, it.toAlertId(rule), Instant.now())
                }
            }
        }

        // Check if doors are open
        if (ruleState.openedContactSensors.isEmpty()) {
            //  no doors open

            // check if 'cancel' message is needed
            if (ruleState.sentAlerts.isNotEmpty()) {
                // TODO: Are cancel messages needed for POC?
                // send cancel message

                // clear event
                ruleStateService.clearByRuleId(rule.id)
            }

        } else if (ruleState.rainForecast == null || Duration.between(
                Instant.now(),
                ruleState.rainForecast.dateTime
            ) < rule.alertingWindow
        ) {
            // check if 'cancel' message is needed
            if (ruleState.sentAlerts.isNotEmpty()) {
                // TODO: Are cancel messages needed for POC?
                // send cancel message

                // clear event
                ruleStateService.clearByRuleId(rule.id)
            }
        } else {
            //  doors open -->
            //      check which alert has been sent (check latest alert (of this type)
            val latestSentAlert: SentAlert? =
                ruleState.sentAlerts.filter { alerts[it.alertId]?.timeToDeadline != null }
                    .maxByOrNull { it.dateTime }

            // Check when it'll rain
            val forecastWithRain: HourlyRainForecast = ruleState.rainForecast
            //          - check time to rain
            //            if below next alert time, sent alert


            val alertsToSend: List<Map.Entry<String, AlertConfig>> =
                rule.alerts.getDeadlineAlertsToSend(latestSentAlert, ruleState)

            // Next alert is:
            //  - An alert for which the timeToDeadline has been passed (time until rain < timeToDeadline)
            //  - An alert should not have been sent before
            //  - The most strict alert should be chosen (if there are two alerts very close to one another, this means one could be skipped)
            val alertToSend = alertsToSend
                .firstOrNull { e ->
                    Duration.between(Instant.now(), forecastWithRain.dateTime) < e.value.timeToDeadline!!
                }

            if (alertToSend != null) {
                log.info("Reached timeLimit for alert ${alertToSend.key} at ${alertToSend.value.timeToDeadline} before rain")
                val properties = getAlertProperties(ruleState, rule, forecastWithRain)
                alertSenderService.send(alertToSend.value, properties)

                return SentAlert(
                    openedContactSensors = ruleState.openedContactSensors,
                    alertId = alertToSend.toAlertId(rule),
                    dateTime = Instant.now()
                )
            }
        }

        return null
    }

    // Next alert is:
    //  - An alert for which the timeToDeadline has been passed (expectedTimeToRain < timeToDeadline)
    //  - An alert should not have been sent before
    //  - The most strict alert should be chosen (if there are two alerts very close to one another, this means one could be skipped)
    /**
     * Alerts with deadline left to send are:
     *  - alerts that have not been sent yet.
     *  - alerts that have a tighter (i.e. shorten, smaller duration) deadline than the latest alert sent
     *
     *  @return a sorted List of the map entries of this map of alerts
     */
    private fun Map<String, AlertConfig>.getDeadlineAlertsToSend(
        latestSentAlert: SentAlert?,
        ruleState: RuleState
    ): List<Map.Entry<String, AlertConfig>> = this.entries
        .filter { e ->
            val alertId = e.toAlertId(ruleState.ruleId)
            val timeToDeadline = e.value.timeToDeadline
            timeToDeadline != null && timeToDeadline < (latestSentAlert?.alertId?.let { alerts[it] }?.timeToDeadline
                ?: Duration.ofDays(1))
                    && !ruleState.sentAlerts.map { a -> a.alertId }.contains(alertId)
        }

        .sortedBy { e -> e.value.timeToDeadline }


    private fun getAlertProperties(
        ruleState: RuleState,
        rule: Rule,
        hourlyForecast: HourlyRainForecast? = null
    ) = mapOf(
        "openContactSensors" to
                ruleState.openedContactSensors.mapNotNull(String::toDeviceName).toString(),
        "allContactSensors" to
                rule.deviceIds.subtract(ruleState.openedContactSensors)
                    .mapNotNull(String::toDeviceName).toString(),
        "timeToRain" to if (hourlyForecast != null) "${
            Duration.between(Instant.now(), hourlyForecast.dateTime).toMinutes()
        }m" else "",
        "precipitationProb" to if (hourlyForecast != null) "${hourlyForecast.precipitationChance * 100}%" else "",
        "precipitationVolume" to if (hourlyForecast != null) "${hourlyForecast.precipitationVolume}mm" else ""
    )

    suspend fun scheduleUpdate(timedUpdateRequest: TimedUpdateRequest): Boolean =
        timedUpdatesEventBus.publish(timedUpdateRequest)
}
