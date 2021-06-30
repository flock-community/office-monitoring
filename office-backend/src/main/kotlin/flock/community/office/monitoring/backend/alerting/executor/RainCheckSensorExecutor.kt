package flock.community.office.monitoring.backend.alerting.executor

import flock.community.office.monitoring.backend.alerting.domain.ContactSensorUpdate
import flock.community.office.monitoring.backend.alerting.domain.RainForecast
import flock.community.office.monitoring.backend.alerting.domain.Rule
import flock.community.office.monitoring.backend.alerting.domain.RuleState
import flock.community.office.monitoring.backend.alerting.domain.RuleStateUpdate
import flock.community.office.monitoring.backend.alerting.domain.RuleType
import flock.community.office.monitoring.backend.alerting.service.evaluators.AlertCheckEvaluator
import flock.community.office.monitoring.backend.alerting.service.evaluators.AlertCheckEvaluatorData
import flock.community.office.monitoring.backend.alerting.service.evaluators.DeviceStateEvaluator
import flock.community.office.monitoring.backend.alerting.service.evaluators.RainForecastEvaluator
import flock.community.office.monitoring.backend.alerting.service.RuleStateService
import flock.community.office.monitoring.backend.alerting.service.TimedUpdateRequest
import flock.community.office.monitoring.utils.logging.loggerFor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import org.springframework.stereotype.Service
import java.time.Instant


@Service
class RainCheckSensorExecutor(
    private val ruleStateService: RuleStateService,
    private val deviceStateEvaluator: DeviceStateEvaluator,
    private val rainForecastEvaluator: RainForecastEvaluator,
    private val alertCheckEvaluator: AlertCheckEvaluator
) : RuleImplExecutor<RuleState> {

    override fun type() = RuleType.RAIN_CHECK_CONTACT_SENSOR

    private val log = loggerFor<RuleImplExecutor<RuleState>>()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun start(rule: Rule): Flow<RuleState> {
        // subscribe to updates / state changes
        val deviceStateUpdates = subscribeToContactSensorUpdates(rule)
        val weatherUpdates = subscribeToRainForecastUpdates(rule)
        val timedUpdates = subscribeToTimedUpdates(rule)

        return merge(deviceStateUpdates, weatherUpdates, timedUpdates)
            .mapNotNull { ruleStateUpdate ->
                guardAll {
                    val currentRuleState = ruleStateService.getActiveRuleState(rule.id)
                    currentRuleState
                        .let { ruleStateUpdate.contactSensorUpdate.handleContactSensorUpdate(it) }
                        .let { ruleStateUpdate.rainForecastUpdate.handleWeatherUpdate(it) }
                        .let { ruleStateUpdate.timedUpdate.handleTimedUpdate(it, rule) }
                        .also {
                            if (it != currentRuleState) {
                                ruleStateService.update(it)

                                alertCheckEvaluator.scheduleUpdate(
                                    TimedUpdateRequest(
                                        Instant.now(),
                                        rule.id,
                                        "RuleState has changed."
                                    )
                                )
                            }
                        }
                }
            }
    }

    private fun subscribeToContactSensorUpdates(rule: Rule): Flow<RuleStateUpdate> =
        deviceStateEvaluator.subscribeToUpdates(rule)
            .map { RuleStateUpdate(contactSensorUpdate = it) }

    private fun subscribeToRainForecastUpdates(rule: Rule): Flow<RuleStateUpdate> =
        rainForecastEvaluator.subscribeToUpdates(rule)
            .map { RuleStateUpdate(rainForecastUpdate = it) }

    private fun subscribeToTimedUpdates(rule: Rule): Flow<RuleStateUpdate> =
        alertCheckEvaluator.subscribeToUpdates(rule)
            .map { RuleStateUpdate(timedUpdate = it) }

    suspend fun ContactSensorUpdate?.handleContactSensorUpdate(ruleState: RuleState): RuleState {
        if (this == null) return ruleState

        val previouslyOpenedContactSensors: Set<String> = ruleState.openedContactSensors
        val updatedContactSensors: Set<String> = deviceStateEvaluator.handleUpdate(this, previouslyOpenedContactSensors)

        return if (previouslyOpenedContactSensors != updatedContactSensors) {
            ruleState.copy(openedContactSensors = updatedContactSensors, lastStateChange = Instant.now())
        } else {
            ruleState
        }
    }

    private suspend fun RainForecast?.handleWeatherUpdate(ruleState: RuleState): RuleState {
        if (this == null) return ruleState

        val previousRainForecast = ruleState.rainForecast
        val newRainForecast = rainForecastEvaluator.handleUpdate(this, previousRainForecast)
        return if (newRainForecast != previousRainForecast) {
            ruleState.copy(rainForecast = newRainForecast, lastStateChange = Instant.now())
        } else {
            ruleState
        }
    }

    private suspend fun TimedUpdateRequest?.handleTimedUpdate(ruleState: RuleState, rule: Rule): RuleState =
        if (this == null) ruleState
        else alertCheckEvaluator.handleUpdate(this, AlertCheckEvaluatorData(ruleState, rule)).ruleState

    private inline fun <T> guardAll(block: () -> T): T? = try {
        block()
    } catch (t: Throwable) {
        log.error(
            "Error occurred clearing state change handling(s) for. This means alerting might run out of sync(!!)",
            t
        )
        // TODO: Trigger a 'reset' after x time (maybe exponential backoff), to do a full restart?
        null
    }
}
