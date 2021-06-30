package flock.community.office.monitoring.backend.alerting.service

import flock.community.office.monitoring.backend.alerting.domain.HourlyRainForecast
import flock.community.office.monitoring.backend.alerting.domain.RuleId
import flock.community.office.monitoring.backend.alerting.domain.RuleState
import flock.community.office.monitoring.backend.alerting.domain.RuleStateId
import flock.community.office.monitoring.backend.alerting.repository.RuleStateEntity
import flock.community.office.monitoring.backend.alerting.repository.RuleStateMapper
import flock.community.office.monitoring.backend.alerting.repository.RuleStateRepository
import flock.community.office.monitoring.utils.logging.loggerFor
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

class RuleStateException(message: String, cause: Throwable) : RuntimeException(message, cause)

@Service
class RuleStateService(
    private val ruleStateRepository: RuleStateRepository,
    private val ruleStateMapper: RuleStateMapper
) {
    private val log = loggerFor<RuleStateService>()

    fun getActiveRuleState(ruleId: RuleId): RuleState =
        try {
            val ruleStateEntity =
                ruleStateRepository.findFirstByRuleIdAndActiveOrderByLastStateChangeDesc(ruleId.value)
                    ?: run {
                        val event = createRuleStateEntity(ruleId)
                        ruleStateRepository.save(
                            event
                        )
                    }
            ruleStateMapper.internalize(ruleStateEntity)
        } catch (t: Throwable) {
            throw RuleStateException("Could not get active RuleState for ${ruleId.value}", t)
        }

    fun createRuleStateEntity(ruleId: RuleId): RuleStateEntity = RuleStateEntity(
        id = UUID.randomUUID().toString(),
        ruleId = ruleId.value,
        active = true,
        openedContactSensors = emptySet(),
        rainForecast = null,
        lastStateChange = Instant.now(),
        sentAlerts = emptyList()
    )

    fun createNewRuleState(ruleId: RuleId, rainForecast: HourlyRainForecast?):RuleState = ruleStateMapper.internalize(createRuleStateEntity(ruleId)).copy(
        rainForecast = rainForecast
    )

    // Probably want this to be atomic or something to deal with race conditions?
    fun update(ruleState: RuleState): RuleState = try {
        val ruleStateEntity = ruleStateMapper.externalize(ruleState)
        val savedEntity = ruleStateRepository.save(ruleStateEntity)
        ruleStateMapper.internalize(savedEntity)
    } catch (t: Throwable) {
        throw RuleStateException("Could not update RuleState for ${ruleState.ruleId.value}", t)
    }

    fun clearByRuleId(id: RuleId): List<RuleStateId> =
        try {
            val ruleStateEntities = ruleStateRepository.findAllByRuleIdAndActive(id.value)
            val updatedStates = ruleStateEntities.map { it.copy(active = false, lastStateChange = Instant.now()) }

            val saveAll: MutableIterable<RuleStateEntity> = ruleStateRepository.saveAll(updatedStates)
            saveAll.map { RuleStateId(it.id) }
        } catch (t: Throwable) {
            log.warn("Error occurred clearing RuleState(s) for $id. Ignoring error for now", t)
            emptyList()
        }
}
