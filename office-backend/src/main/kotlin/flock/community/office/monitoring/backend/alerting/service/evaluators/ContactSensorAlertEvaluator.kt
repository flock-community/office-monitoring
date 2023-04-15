package flock.community.office.monitoring.backend.alerting.service.evaluators

import flock.community.office.monitoring.backend.alerting.domain.Alert
import flock.community.office.monitoring.backend.alerting.domain.AlertConfig
import flock.community.office.monitoring.backend.alerting.domain.AlertId
import flock.community.office.monitoring.backend.alerting.domain.Rule
import flock.community.office.monitoring.backend.alerting.domain.AlertState
import flock.community.office.monitoring.backend.alerting.domain.SentAlert
import flock.community.office.monitoring.backend.alerting.domain.toAlertId
import flock.community.office.monitoring.backend.alerting.domain.ContactSensorAlertData
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


@Component
class ContactSensorAlertEvaluator(
    private val timedUpdatesEventBus: TimedUpdatesEventBus,
    private val alertSenderService: AlertSenderService,
    private val alertStateService: AlertStateService,
    @Qualifier("alerts") private val alerts: Map<AlertId, Alert>,
) {

    private val log = loggerFor<ContactSensorAlertEvaluator>()

    fun subscribeToUpdates(rule: Rule): Flow<TimedUpdateRequest> {
        return timedUpdatesEventBus.subscribe(rule)
    }

    suspend fun handleUpdate(
        rule: Rule,
        contactSensorAlertData: ContactSensorAlertData,
        previousAlertState: AlertState,
    ): AlertState {
        if (canAlertStateBeReset(rule, contactSensorAlertData, previousAlertState)) {
            return resetAlertState(rule, contactSensorAlertData)
        }

        val alertToSend: Alert? = evaluateAlerts(previousAlertState, contactSensorAlertData, rule)
        return if (alertToSend != null) {
            val sentAlert = sendAlert(alertToSend, contactSensorAlertData, rule)
            previousAlertState.copy(
                sentAlerts = previousAlertState.sentAlerts + sentAlert,
                lastStateChange = Instant.now()
            )
        } else {
            previousAlertState
        }
    }

    private suspend fun sendAlert(
        alertToSend: Alert,
        rainCheckSensorData: ContactSensorAlertData,
        rule: Rule
    ): SentAlert {
        log.info("Reached correct state for alert ${alertToSend.alertId.value}")
        val properties = getAlertProperties(rule)
        alertSenderService.send(alertToSend, properties)

        return SentAlert(
            alertId = alertToSend.alertId,
            dateTime = Instant.now()
        )
    }

    private suspend fun resetAlertState(
        rule: Rule,
        rainCheckSensorData: ContactSensorAlertData
    ): AlertState {
        alertStateService.clearByRuleId(rule.id)
        val createNewAlertState = alertStateService.createNewAlertState(rule.id)

        log.info("Sending cancel message for rule ${rule.id.value}")
        val alertToSend = rule.cancelMessage
        val properties = getAlertProperties(rule)
        alertSenderService.send(alertToSend, properties)
        return createNewAlertState
    }

    private fun canAlertStateBeReset(
        rule: Rule,
        contactSensorAlertData: ContactSensorAlertData,
        previousAlertState: AlertState
    ): Boolean {
        val haveSentAlertsBefore = previousAlertState.sentAlerts.isNotEmpty()
        val allContactSensorsClosed = contactSensorAlertData.openedContactSensors.isEmpty()

        return haveSentAlertsBefore && (allContactSensorsClosed )
            .also {
                if (it) {
                    log.debug(
                        "RuleState can be reset (apparently): " +
                                "previousAlertState: $previousAlertState, " +
                                "rainCheckSensorData: $contactSensorAlertData, " +
                                "rule: $rule"
                    )
                }
            }
    }

    private fun evaluateAlerts(
        alertState: AlertState,
        contactSensorAlertData: ContactSensorAlertData,
        rule: Rule
    ): Alert? {
        // Check if alerts are needed
        if (contactSensorAlertData.openedContactSensors.isEmpty()) return null

        // check which alert has been sent (check latest alert (of this type)
        val latestSentAlert: SentAlert? = alertState.sentAlerts.maxByOrNull { it.dateTime }
        val alertsToSend: List<ConfiguredAlert> = rule.alerts.getAlertsToSend(latestSentAlert, alertState)
        val alertToSend: ConfiguredAlert? = alertsToSend.firstOrNull()

        return alertToSend?.let {
            val alertId = alertToSend.toAlertId(rule.id)
            alerts[alertId]
        }
    }

    /**
     * Alerts with deadline left to send are:
     *  - alerts that have not been sent yet.
     *  - alerts that have a tighter (i.e. shorter, smaller duration) deadline than the latest alert sent
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
        rule: Rule
    ): Map<String, String> {
        return mapOf(
            "deviceName" to (rule.deviceIds.firstOrNull()?.let(String::toDeviceName) ?: "<????>"),
        )
    }

}
