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
import flock.community.office.monitoring.backend.alerting.service.TimedUpdateRequest
import flock.community.office.monitoring.backend.alerting.service.TimedUpdatesEventBus
import flock.community.office.monitoring.backend.device.configuration.toDeviceName
import flock.community.office.monitoring.utils.logging.loggerFor
import kotlinx.coroutines.flow.Flow
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

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

        val sentAlerts: List<SentAlert> = listOfNotNull(
            // FIXME: Quick hack to report on changes in open/closed contact sensors
            trySendContactSensorChangeAlert(previousStateValue.ruleState, previousStateValue.rule),
            trySendWeatherUpdateAlert(previousStateValue.ruleState, previousStateValue.rule)
        )

        if (sentAlerts.isNotEmpty()) {
            return AlertCheckEvaluatorData(
                previousStateValue.ruleState.copy(
                    sentAlerts = previousStateValue.ruleState.sentAlerts + sentAlerts,
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
            val alert: Alert? = alerts.entries.find {
                it.value.alertId.value.contains("every-contact-change") && it.value.alertId.value.contains(rule.id.value)
            }?.value

            alert?.let {
                val properties = getAlertProperties(ruleState, rule)
                alertSenderService.send(it, properties)


                return SentAlert(
                    openedContactSensors = ruleState.openedContactSensors,
                    alertId = it.alertId,
                    dateTime = Instant.now()
                )

            }
        }
        return null
    }

    private suspend fun trySendWeatherUpdateAlert(ruleState: RuleState, rule: Rule): SentAlert? {
        // Check if alerts are needed
        if (ruleState.openedContactSensors.isEmpty()
            || ruleState.rainForecast == null
            || Duration.between(Instant.now(), ruleState.rainForecast.dateTime) > rule.alertingWindow
        ) {
            //  no alerts needed

            // check if 'cancel' message is needed
            if (ruleState.sentAlerts.isNotEmpty()) {
                // TODO: Are cancel messages needed for POC?
                // send cancel message

                // clear event
                ruleStateService.clearByRuleId(rule.id)
            }
        } else {
            // check which alert has been sent (check latest alert (of this type)
            val latestSentAlert: SentAlert? = ruleState.sentAlerts.maxByOrNull { it.dateTime }
            val alertsToSend = rule.alerts.getAlertsToSend(latestSentAlert, ruleState)
            val alertToSend = alertsToSend.firstOrNull { e ->
                Duration.between(Instant.now(), ruleState.rainForecast.dateTime) < e.value.timeToDeadline
            }

            if (alertToSend != null) {
                log.info("Reached timeLimit for alert ${alertToSend.key} at ${alertToSend.value.timeToDeadline} before rain")
                val properties = getAlertProperties(ruleState, rule, ruleState.rainForecast)
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

    /**
     * Alerts with deadline left to send are:
     *  - alerts that have not been sent yet.
     *  - alerts that have a tighter (i.e. shorten, smaller duration) deadline than the latest alert sent
     *
     *  @return a sorted List of the map entries of this map of alerts
     */
    private fun Map<String, AlertConfig>.getAlertsToSend(
        latestSentAlert: SentAlert?,
        ruleState: RuleState
    ): List<Map.Entry<String, AlertConfig>> {
        val timeToDeadlineOfLatestSentAlert = (latestSentAlert?.alertId?.let { alerts[it] }?.timeToDeadline
            ?: Duration.ofDays(1))

        return this.entries
            .filter { e ->
                val alertId = e.toAlertId(ruleState.ruleId)
                !ruleState.sentAlerts.map { a -> a.alertId }.contains(alertId)
                        && e.value.timeToDeadline < timeToDeadlineOfLatestSentAlert
            }
            .sortedBy { e -> e.value.timeToDeadline }
    }


    private fun getAlertProperties(
        ruleState: RuleState,
        rule: Rule,
        hourlyForecast: HourlyRainForecast? = null
    ) = mapOf(
        "openContactSensors" to ruleState.openedContactSensors.mapNotNull(String::toDeviceName).toString(),
        "closedContactSensors" to
                rule.deviceIds.subtract(ruleState.openedContactSensors).mapNotNull(String::toDeviceName).toString(),
        "timeToRain" to if (hourlyForecast != null)
            "${Duration.between(Instant.now(), hourlyForecast.dateTime).toMinutes()}m" else ">9000m",
        "precipitationProb" to "${hourlyForecast?.precipitationChance?.times(100) ?: "?"}%",
        "precipitationVolume" to "${hourlyForecast?.precipitationVolume ?: "?"}mm"
    )

    suspend fun scheduleUpdate(timedUpdateRequest: TimedUpdateRequest): Boolean =
        timedUpdatesEventBus.publish(timedUpdateRequest)
}
