package flock.community.office.monitoring.backend.alerting.service

import flock.community.office.monitoring.backend.alerting.domain.RuleId
import flock.community.office.monitoring.backend.alerting.executor.RainAlertData
import flock.community.office.monitoring.backend.alerting.repository.RainAlertEntity
import flock.community.office.monitoring.backend.alerting.repository.RainAlertMapper
import flock.community.office.monitoring.backend.alerting.repository.RainAlertRepository
import flock.community.office.monitoring.utils.logging.loggerFor
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

class RainAlertDataException(message: String, cause: Throwable) : RuntimeException(message, cause)

@Service
class RainAlertService(
    private val repository: RainAlertRepository,
    private val mapper: RainAlertMapper
) {
    private val log = loggerFor<AlertStateService>()

    fun getDataForRule(ruleId: RuleId): RainAlertData =
        try {
            val rainAlertEntity =
                repository.findByRuleId(ruleId.value)
                    ?: run {
                        val event = createRainAlertEntity(ruleId)
                        repository.save(
                            event
                        )
                    }
            mapper.internalize(rainAlertEntity)
        } catch (t: Throwable) {
            throw RainAlertDataException("Could not get active RainAlertData for ${ruleId.value}", t)
        }

    fun createRainAlertEntity(ruleId: RuleId): RainAlertEntity = RainAlertEntity(
        id = UUID.randomUUID().toString(),
        ruleId = ruleId.value,
        openedContactSensors = emptySet(),// Change me for testing:  setOf("always-open"),
        rainForecast = null,
        lastStateChange = Instant.EPOCH
    )

    // Probably want this to be atomic or something to deal with race conditions?
    fun update(rainAlertData: RainAlertData): RainAlertData = try {
        val rainAlertEntity = mapper.externalize(rainAlertData)
        val savedEntity = repository.save(rainAlertEntity)
        mapper.internalize(savedEntity)
    } catch (t: Throwable) {
        throw RainAlertDataException("Could not update RainAlertData for ${rainAlertData.ruleId.value}", t)
    }
}
