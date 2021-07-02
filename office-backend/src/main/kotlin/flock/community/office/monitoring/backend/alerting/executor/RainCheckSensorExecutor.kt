package flock.community.office.monitoring.backend.alerting.executor

import flock.community.office.monitoring.backend.alerting.domain.ContactSensorUpdate
import flock.community.office.monitoring.backend.alerting.domain.HourlyRainForecast
import flock.community.office.monitoring.backend.alerting.domain.RainForecast
import flock.community.office.monitoring.backend.alerting.domain.Rule
import flock.community.office.monitoring.backend.alerting.domain.RuleId
import flock.community.office.monitoring.backend.alerting.domain.AlertState
import flock.community.office.monitoring.backend.alerting.domain.RainCheckSensorUpdate
import flock.community.office.monitoring.backend.alerting.domain.RuleType
import flock.community.office.monitoring.backend.alerting.service.RainCheckSensorDataService
import flock.community.office.monitoring.backend.alerting.service.evaluators.AlertCheckEvaluator
import flock.community.office.monitoring.backend.alerting.service.evaluators.DeviceStateEvaluator
import flock.community.office.monitoring.backend.alerting.service.evaluators.RainForecastEvaluator
import flock.community.office.monitoring.backend.alerting.service.AlertStateService
import flock.community.office.monitoring.backend.alerting.service.TimedUpdateRequest
import flock.community.office.monitoring.utils.logging.loggerFor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import org.springframework.stereotype.Service
import java.time.Instant

@JvmInline
value class RainCheckSensorDataId(val value: String)
data class RainCheckSensorData(
    val id: RainCheckSensorDataId,
    val ruleId: RuleId,
    val openedContactSensors: Set<String>,
    val rainForecast: HourlyRainForecast?,
    val lastStateChange: Instant,
)

@Service
class RainCheckSensorExecutor(
    private val alertStateService: AlertStateService,
    private val rainCheckSensorDataService: RainCheckSensorDataService,
    private val deviceStateEvaluator: DeviceStateEvaluator,
    private val rainForecastEvaluator: RainForecastEvaluator,
    private val alertCheckEvaluator: AlertCheckEvaluator
) : RuleImplExecutor<AlertState> {

    override fun type() = RuleType.RAIN_CHECK_CONTACT_SENSOR

    private val log = loggerFor<RuleImplExecutor<AlertState>>()

    override fun start(rule: Rule): Flow<AlertState> {
        // subscribe to updates / state changes
        val deviceStateUpdates: Flow<ContactSensorUpdate> = subscribeToContactSensorUpdates(rule)
        val weatherUpdates: Flow<RainForecast> = subscribeToRainForecastUpdates(rule)

        val rainCheckSensorDataState = evaluateRule(deviceStateUpdates, weatherUpdates, rule.id)

        val timedUpdates: Flow<TimedUpdateRequest> = subscribeToTimedUpdates(rule)
        return evaluateAlerts(rainCheckSensorDataState, timedUpdates, rule)
    }

    private fun evaluateAlerts(
        rainCheckSensorDataState: Flow<RainCheckSensorData>,
        timedUpdates: Flow<TimedUpdateRequest>,
        rule: Rule
    ): Flow<AlertState> = rainCheckSensorDataState
        .combine(timedUpdates)
        { rainCheckSensorData, stateUpdate ->
            log.info("LatestRainCheckSensorData: $rainCheckSensorData, Latest TimedUpdateRequest: $stateUpdate")

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
        weatherUpdates: Flow<RainForecast>,
        ruleId: RuleId
    ): Flow<RainCheckSensorData> =
        merge(
            deviceStateUpdates.map { RainCheckSensorUpdate(contactSensorUpdate = it) },
            weatherUpdates.map { RainCheckSensorUpdate(rainForecastUpdate = it) }
        )
            .scan(rainCheckSensorDataService.getDataForRule(ruleId))
            { currentState, update ->
                currentState
                    .let { update.contactSensorUpdate.handleContactSensorUpdate(it) }
                    .let { update.rainForecastUpdate.handleWeatherUpdate(it) }
                    .also {
                        if (it != currentState) {
                            log.debug("rainCheckSensorData state has changed. Saving state. New state: $it")
                            guardAll {
                                rainCheckSensorDataService.update(it)
                            }
                        }
                    }
            }

    private fun subscribeToContactSensorUpdates(rule: Rule): Flow<ContactSensorUpdate> =
        deviceStateEvaluator.subscribeToUpdates(rule)


    private fun subscribeToRainForecastUpdates(rule: Rule): Flow<RainForecast> =
        rainForecastEvaluator.subscribeToUpdates(rule)

    private fun subscribeToTimedUpdates(rule: Rule): Flow<TimedUpdateRequest> =
        alertCheckEvaluator.subscribeToUpdates(rule)
            .onEach { log.info("Resolving timedUpdate because: ${it.triggerReason}") }

    suspend fun ContactSensorUpdate?.handleContactSensorUpdate(rainCheckSensorData: RainCheckSensorData): RainCheckSensorData {
        if (this == null) return rainCheckSensorData

        val previouslyOpenedContactSensors: Set<String> = rainCheckSensorData.openedContactSensors
        val updatedContactSensors: Set<String> = deviceStateEvaluator.handleUpdate(this, previouslyOpenedContactSensors)

        return if (previouslyOpenedContactSensors != updatedContactSensors) {
            rainCheckSensorData.copy(openedContactSensors = updatedContactSensors, lastStateChange = Instant.now())
        } else {
            rainCheckSensorData
        }
    }

    private suspend fun RainForecast?.handleWeatherUpdate(rainCheckSensorData: RainCheckSensorData): RainCheckSensorData {
        if (this == null) return rainCheckSensorData

        val previousRainForecast = rainCheckSensorData.rainForecast
        val newRainForecast = rainForecastEvaluator.handleUpdate(this, previousRainForecast)

        return if (newRainForecast != previousRainForecast) {
            rainCheckSensorData.copy(rainForecast = newRainForecast, lastStateChange = Instant.now())
        } else {
            rainCheckSensorData
        }
    }


    private inline fun <T> guardAll(block: () -> T): T? = try {
        block()
    } catch (t: Throwable) {
        log.error(
            "Unexpected error occurred. This means alerting might run out of sync(!!)",
            t
        )
        // TODO: Trigger a 'reset' after x time (maybe exponential backoff), to do a full restart?
        null
    }
}
