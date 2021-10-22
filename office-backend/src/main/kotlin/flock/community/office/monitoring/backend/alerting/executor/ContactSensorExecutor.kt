package flock.community.office.monitoring.backend.alerting.executor

import flock.community.office.monitoring.backend.alerting.domain.AlertState
import flock.community.office.monitoring.backend.alerting.domain.ContactSensorAlertData
import flock.community.office.monitoring.backend.alerting.domain.ContactSensorUpdate
import flock.community.office.monitoring.backend.alerting.domain.Rule
import flock.community.office.monitoring.backend.alerting.domain.RuleId
import flock.community.office.monitoring.backend.alerting.domain.RuleType
import flock.community.office.monitoring.backend.alerting.service.AlertStateService
import flock.community.office.monitoring.backend.alerting.service.ContactSensorAlertingDataService
import flock.community.office.monitoring.backend.alerting.service.TimedUpdateRequest
import flock.community.office.monitoring.backend.alerting.service.evaluators.ContactSensorAlertEvaluator
import flock.community.office.monitoring.backend.alerting.service.evaluators.DeviceStateEvaluator
import flock.community.office.monitoring.utils.logging.loggerFor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import org.springframework.stereotype.Service
import java.time.Instant


/**
 * ContactSensor is a simple executor that works with a single configured device. It alerts when contact is broken ('open')
 * and when made ('closed). If the device breaks contact, it is considered to be in Alert state
 */
@Service
class ContactSensorExecutor(
    private val alertStateService: AlertStateService,
    private val contactSensorAlertingDataService: ContactSensorAlertingDataService,
    private val deviceStateEvaluator: DeviceStateEvaluator,
    private val alertCheckEvaluator: ContactSensorAlertEvaluator
) : RuleImplExecutor<AlertState> {

    override fun type() = RuleType.CONTACT_SENSOR

    private val log = loggerFor<RuleImplExecutor<AlertState>>()

    override fun start(rule: Rule): Flow<AlertState> {
        if (rule.deviceIds.size != 1){
            val message =
                "Rule '${rule.name}' (id ${rule.id.value}, is configured INCORRECTLY. Exactly 1 device id should be configured, but was ${rule.deviceIds}"
            log.error(message)

            throw IllegalArgumentException(message);
        }

        // subscribe to updates / state changes
        val deviceStateUpdates: Flow<ContactSensorUpdate> = subscribeToContactSensorUpdates(rule)
        val sensorDataState = evaluateRule(deviceStateUpdates, rule.id)

        val timedUpdates: Flow<TimedUpdateRequest> = subscribeToTimedUpdates(rule)
        return evaluateAlerts(sensorDataState, timedUpdates, rule)
    }

    private fun evaluateAlerts(
        contactSensorDataState: Flow<ContactSensorAlertData>,
        timedUpdates: Flow<TimedUpdateRequest>,
        rule: Rule
    ): Flow<AlertState> = contactSensorDataState
        .combine(timedUpdates)
        { rainCheckSensorData, stateUpdate ->
            log.info("LatestContactSensorAlertData: $rainCheckSensorData, Latest TimedUpdateRequest: $stateUpdate")

            guardAll {
                val previousAlertState = alertStateService.getActiveRuleState(rule.id)

                alertCheckEvaluator.handleUpdate(rule, rainCheckSensorData, previousAlertState)
                    .also {
                        if (it != previousAlertState) {
                            alertStateService.update(it)
                        }
                    }
            }
        }.filterNotNull()

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun evaluateRule(
        deviceStateUpdates: Flow<ContactSensorUpdate>,
        ruleId: RuleId
    ): Flow<ContactSensorAlertData> =
            deviceStateUpdates
            .scan(contactSensorAlertingDataService.getDataForRule(ruleId))
            { currentState, update ->
                currentState
                    .let { update.handleContactSensorUpdate(it) }
                    .also {
                        if (it != currentState) {
                            log.debug("ContactSensorAlertingData state has changed. Saving state. New state: $it")
                            guardAll {
                                contactSensorAlertingDataService.update(it)
                            }
                        }
                    }
            }

    private fun subscribeToContactSensorUpdates(rule: Rule): Flow<ContactSensorUpdate> =
        deviceStateEvaluator.subscribeToUpdates(rule)


    private fun subscribeToTimedUpdates(rule: Rule): Flow<TimedUpdateRequest> =
        alertCheckEvaluator.subscribeToUpdates(rule)
            .onEach { log.info("Resolving timedUpdate because: ${it.triggerReason}") }

    suspend fun ContactSensorUpdate.handleContactSensorUpdate(contactSensorAlertData: ContactSensorAlertData): ContactSensorAlertData {
        if (this == null) return contactSensorAlertData

        val previouslyOpenedContactSensors: Set<String> = contactSensorAlertData.openedContactSensors
        val updatedContactSensors: Set<String> = deviceStateEvaluator.handleUpdate(this, previouslyOpenedContactSensors)

        return if (previouslyOpenedContactSensors != updatedContactSensors) {
            contactSensorAlertData.copy(openedContactSensors = updatedContactSensors, lastStateChange = Instant.now())
        } else {
            contactSensorAlertData
        }
    }

    private inline fun <T> guardAll(block: () -> T): T? = try {
        block()
    } catch (t: Throwable) {
        log.error("Unexpected error occurred. This means alerting might run out of sync(!!)", t)
        // TODO: Trigger a 'reset' after x time (maybe exponential backoff), to do a full restart?
        null
    }
}
