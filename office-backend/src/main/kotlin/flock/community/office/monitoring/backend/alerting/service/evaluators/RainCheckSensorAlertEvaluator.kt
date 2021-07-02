package flock.community.office.monitoring.backend.alerting.service.evaluators

import flock.community.office.monitoring.backend.alerting.domain.Alert
import flock.community.office.monitoring.backend.alerting.domain.AlertConfig
import flock.community.office.monitoring.backend.alerting.domain.AlertId
import flock.community.office.monitoring.backend.alerting.domain.Rule
import flock.community.office.monitoring.backend.alerting.domain.AlertState
import flock.community.office.monitoring.backend.alerting.domain.SentAlert
import flock.community.office.monitoring.backend.alerting.domain.toAlertId
import flock.community.office.monitoring.backend.alerting.executor.RainCheckSensorData
import flock.community.office.monitoring.backend.alerting.service.AlertSenderService
import flock.community.office.monitoring.backend.alerting.service.AlertStateService
import flock.community.office.monitoring.backend.alerting.service.TimedUpdateRequest
import flock.community.office.monitoring.backend.alerting.service.TimedUpdatesEventBus
import flock.community.office.monitoring.backend.device.configuration.toDeviceName
import flock.community.office.monitoring.utils.logging.loggerFor
import kotlinx.coroutines.flow.Flow
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

data class RainCheckSensorAlertCheckData(
    val rule: Rule,
    val alertState: AlertState,
    val rainCheckSensorData: RainCheckSensorData
)

@Component
class AlertCheckEvaluator(
    private val timedUpdatesEventBus: TimedUpdatesEventBus,
    private val alertSenderService: AlertSenderService,
    private val alertStateService: AlertStateService,
    @Qualifier("alerts") private val alerts: Map<AlertId, Alert>,
) {

    private val log = loggerFor<AlertCheckEvaluator>()

    fun subscribeToUpdates(rule: Rule): Flow<TimedUpdateRequest> {
        return timedUpdatesEventBus.subscribe(rule)
    }

    suspend fun handleUpdate(
        update: TimedUpdateRequest,
        rule: Rule,
        rainCheckSensorData: RainCheckSensorData,
        previousAlertState: AlertState,
    ): AlertState {
        if (canAlertStateBeReset(rule, rainCheckSensorData, previousAlertState)) {
            log.debug(
                "RuleState can be reset (apparently): " +
                        "previousAlertState: $previousAlertState, " +
                        "rainCheckSensorData: $rainCheckSensorData, " +
                        "rule: $rule"
            )
            alertStateService.clearByRuleId(rule.id)
            val createNewAlertState = alertStateService.createNewAlertState(rule.id)

            log.info("Sending cancel message for rule ${rule.id.value}")
            val alertToSend = rule.cancelMessage
            val properties = getAlertProperties(rainCheckSensorData, rule)
            alertSenderService.send(alertToSend, properties)

            return createNewAlertState
        }

        val sentAlert: SentAlert? = trySendWeatherUpdateAlert(previousAlertState, rule, rainCheckSensorData)

        return if (sentAlert != null) {
            previousAlertState.copy(
                sentAlerts = previousAlertState.sentAlerts + sentAlert,
                lastStateChange = Instant.now()
            )
        } else {
            previousAlertState
        }
    }

    private fun canAlertStateBeReset(
        rule: Rule,
        rainCheckSensorData: RainCheckSensorData,
        previousAlertState: AlertState
    ): Boolean {
        val haveSentAlertsBefore = previousAlertState.sentAlerts.isNotEmpty()

        val allContactSensorsClosed = rainCheckSensorData.openedContactSensors.isEmpty()
        val rainForecast = rainCheckSensorData.rainForecast
        val rainExpectedOutsideAlertingWindow = rainForecast != null
                && Duration.between(Instant.now(), rainForecast.dateTime) > rule.alertingWindow

        return haveSentAlertsBefore && (allContactSensorsClosed || rainExpectedOutsideAlertingWindow)
    }

    private suspend fun trySendWeatherUpdateAlert(
        alertState: AlertState,
        rule: Rule,
        rainCheckSensorData: RainCheckSensorData
    ): SentAlert? {
        // Check if alerts are needed
        if (rainCheckSensorData.openedContactSensors.isEmpty() || rainCheckSensorData.rainForecast == null) return null

        // check which alert has been sent (check latest alert (of this type)
        val latestSentAlert: SentAlert? = alertState.sentAlerts.maxByOrNull { it.dateTime }
        val alertsToSend = rule.alerts.getAlertsToSend(latestSentAlert, alertState)
        val alertToSend = alertsToSend.firstOrNull { e ->
            Duration.between(Instant.now(), rainCheckSensorData.rainForecast.dateTime) < e.value.timeToDeadline
        }

        if (alertToSend != null) {
            log.info("Reached timeLimit for alert ${alertToSend.key} at ${alertToSend.value.timeToDeadline} before rain")
            val properties = getAlertProperties(rainCheckSensorData, rule)
            alertSenderService.send(alertToSend.value, properties)

            return SentAlert(
                alertId = alertToSend.toAlertId(rule),
                dateTime = Instant.now()
            )
        } else {
            log.debug(
                "Not sending an alert for Rule ${rule.id.value}. " +
                        "OpenContactSensors: ${rainCheckSensorData.openedContactSensors}, " +
                        "RainForecast: ${rainCheckSensorData.rainForecast}. " +
                        "AlertsToSend: $alertsToSend"
            )

            return null
        }
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
        alertState: AlertState
    ): List<Map.Entry<String, AlertConfig>> {
        val timeToDeadlineOfLatestSentAlert = (latestSentAlert?.alertId?.let { alerts[it] }?.timeToDeadline
            ?: Duration.ofDays(1))

        return this.entries
            .filter { e ->
                val alertId = e.toAlertId(alertState.ruleId)
                !alertState.sentAlerts.map { a -> a.alertId }.contains(alertId)
                        && e.value.timeToDeadline < timeToDeadlineOfLatestSentAlert
            }
            .sortedBy { e -> e.value.timeToDeadline }
    }


    private fun getAlertProperties(
        rainCheckSensorData: RainCheckSensorData,
        rule: Rule
    ) = mapOf(
        "openContactSensors" to rainCheckSensorData.openedContactSensors.mapNotNull(String::toDeviceName).toString(),
        "closedContactSensors" to
                rule.deviceIds.subtract(rainCheckSensorData.openedContactSensors).mapNotNull(String::toDeviceName)
                    .toString(),
        "timeToRain" to if (rainCheckSensorData.rainForecast != null)
            "${Duration.between(Instant.now(), rainCheckSensorData.rainForecast.dateTime).toMinutes()}m" else ">9000m",
        "precipitationProb" to "${rainCheckSensorData.rainForecast?.precipitationChance?.times(100) ?: "?"}%",
        "precipitationVolume" to "${rainCheckSensorData.rainForecast?.precipitationVolume ?: "?"}mm"
    )

    suspend fun scheduleUpdate(timedUpdateRequest: TimedUpdateRequest): Boolean =
        timedUpdatesEventBus.publish(timedUpdateRequest)
}
