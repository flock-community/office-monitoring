package flock.community.office.monitoring.backend.alerting.executor

import flock.community.office.monitoring.backend.alerting.domain.ContactSensorUpdate
import flock.community.office.monitoring.backend.alerting.domain.HourlyRainForecast
import flock.community.office.monitoring.backend.alerting.domain.RainForecast
import flock.community.office.monitoring.backend.alerting.domain.Rule
import flock.community.office.monitoring.backend.alerting.domain.RuleId
import flock.community.office.monitoring.backend.alerting.domain.AlertState
import flock.community.office.monitoring.backend.alerting.domain.RainAlertUpdate
import flock.community.office.monitoring.backend.alerting.domain.RuleType
import flock.community.office.monitoring.backend.alerting.service.RainAlertService
import flock.community.office.monitoring.backend.alerting.service.evaluators.RainAlertEvaluator
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
value class RainAlertDataId(val value: String)
data class RainAlertData(
    val id: RainAlertDataId,
    val ruleId: RuleId,
    val openedContactSensors: Set<String>,
    val rainForecast: HourlyRainForecast?,
    val lastStateChange: Instant,
)

@Service
class RainAlertExecutor(
    private val alertStateService: AlertStateService,
    private val rainAlertService: RainAlertService,
    private val deviceStateEvaluator: DeviceStateEvaluator,
    private val rainForecastEvaluator: RainForecastEvaluator,
    private val rainAlertEvaluator: RainAlertEvaluator
) : RuleImplExecutor<AlertState> {

    override fun type() = RuleType.RAIN_CHECK_CONTACT_SENSOR

    private val log = loggerFor<RuleImplExecutor<AlertState>>()

    override fun start(rule: Rule): Flow<AlertState> {
        // subscribe to updates / state changes
        val deviceStateUpdates: Flow<ContactSensorUpdate> = subscribeToContactSensorUpdates(rule)
        val weatherUpdates: Flow<RainForecast> = subscribeToRainForecastUpdates(rule)

        val rainAlertDataState = evaluateRule(deviceStateUpdates, weatherUpdates, rule.id)

        val timedUpdates: Flow<TimedUpdateRequest> = subscribeToTimedUpdates(rule)
        return evaluateAlerts(rainAlertDataState, timedUpdates, rule)
    }

    private fun evaluateAlerts(
        rainAlertDataState: Flow<RainAlertData>,
        timedUpdates: Flow<TimedUpdateRequest>,
        rule: Rule
    ): Flow<AlertState> = rainAlertDataState
        .combine(timedUpdates)
        { rainAlertData, stateUpdate ->
            log.info("LatestRainAlertData: $rainAlertData, Latest TimedUpdateRequest: $stateUpdate")

            guardAll {
                val previousAlertState = alertStateService.getActiveRuleState(rule.id)

                rainAlertEvaluator.handleUpdate(rule, rainAlertData, previousAlertState)
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
): Flow<RainAlertData> =
    merge(
        deviceStateUpdates.map { RainAlertUpdate(contactSensorUpdate = it) },
        weatherUpdates.map { RainAlertUpdate(rainForecastUpdate = it) }
    )
        .scan(rainAlertService.getDataForRule(ruleId))
        { currentState, update ->
            currentState
                .let { update.contactSensorUpdate.handleContactSensorUpdate(it) }
                .let { update.rainForecastUpdate.handleWeatherUpdate(it) }
                .also {
                    if (it != currentState) {
                        log.debug("rainAlertData state has changed. Saving state. New state: $it")
                        guardAll {
                            rainAlertService.update(it)
                        }
                    }
                }
        }

    private fun subscribeToContactSensorUpdates(rule: Rule): Flow<ContactSensorUpdate> =
        deviceStateEvaluator.subscribeToUpdates(rule)


    private fun subscribeToRainForecastUpdates(rule: Rule): Flow<RainForecast> =
        rainForecastEvaluator.subscribeToUpdates(rule)

    private fun subscribeToTimedUpdates(rule: Rule): Flow<TimedUpdateRequest> =
        rainAlertEvaluator.subscribeToUpdates(rule)
            .onEach { log.info("Resolving timedUpdate because: ${it.triggerReason}") }

    suspend fun ContactSensorUpdate?.handleContactSensorUpdate(rainAlertData: RainAlertData): RainAlertData {
        if (this == null) return rainAlertData

        val previouslyOpenedContactSensors: Set<String> = rainAlertData.openedContactSensors
        val updatedContactSensors: Set<String> = deviceStateEvaluator.handleUpdate(this, previouslyOpenedContactSensors)

        return if (previouslyOpenedContactSensors != updatedContactSensors) {
            rainAlertData.copy(openedContactSensors = updatedContactSensors, lastStateChange = Instant.now())
        } else {
            rainAlertData
        }
    }

    private suspend fun RainForecast?.handleWeatherUpdate(rainAlertData: RainAlertData): RainAlertData {
        if (this == null) return rainAlertData

        val previousRainForecast = rainAlertData.rainForecast
        val newRainForecast = rainForecastEvaluator.handleUpdate(this, previousRainForecast)

        return if (newRainForecast != previousRainForecast) {
            rainAlertData.copy(rainForecast = newRainForecast, lastStateChange = Instant.now())
        } else {
            rainAlertData
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
