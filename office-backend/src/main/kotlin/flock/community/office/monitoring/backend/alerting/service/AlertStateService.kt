package flock.community.office.monitoring.backend.alerting.service

import flock.community.office.monitoring.backend.alerting.domain.RuleId
import flock.community.office.monitoring.backend.alerting.domain.AlertState
import flock.community.office.monitoring.backend.alerting.domain.AlertStateId
import flock.community.office.monitoring.backend.alerting.repository.RuleStateEntity
import flock.community.office.monitoring.backend.alerting.repository.RuleStateMapper
import flock.community.office.monitoring.backend.alerting.repository.RuleStateRepository
import flock.community.office.monitoring.utils.logging.loggerFor
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

class RuleStateException(message: String, cause: Throwable) : RuntimeException(message, cause)

@Service
class AlertStateService(
    private val ruleStateRepository: RuleStateRepository,
    private val ruleStateMapper: RuleStateMapper
) {
    private val log = loggerFor<AlertStateService>()

    fun getActiveRuleState(ruleId: RuleId): AlertState =
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
        lastStateChange = Instant.EPOCH,
        sentAlerts = emptyList()
    )

    fun createNewAlertState(ruleId: RuleId): AlertState = ruleStateMapper.internalize(createRuleStateEntity(ruleId))

    // Probably want this to be atomic or something to deal with race conditions?
    fun update(alertState: AlertState): AlertState = try {
        val ruleStateEntity = ruleStateMapper.externalize(alertState)
        val savedEntity = ruleStateRepository.save(ruleStateEntity)
        ruleStateMapper.internalize(savedEntity)
    } catch (t: Throwable) {
        throw RuleStateException("Could not update RuleState for ${alertState.ruleId.value}", t)
    }

    fun clearByRuleId(id: RuleId): List<AlertStateId> =
        try {
            val ruleStateEntities = ruleStateRepository.findAllByRuleIdAndActive(id.value)
            val updatedStates = ruleStateEntities.map { it.copy(active = false, lastStateChange = Instant.now()) }

            val saveAll: MutableIterable<RuleStateEntity> = ruleStateRepository.saveAll(updatedStates)
            saveAll.map { AlertStateId(it.id) }
        } catch (t: Throwable) {
            log.warn("Error occurred clearing RuleState(s) for $id. Ignoring error for now", t)
            emptyList()
        }
}
