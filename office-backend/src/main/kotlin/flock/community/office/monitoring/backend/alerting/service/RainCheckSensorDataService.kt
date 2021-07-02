package flock.community.office.monitoring.backend.alerting.service

import flock.community.office.monitoring.backend.alerting.domain.RuleId
import flock.community.office.monitoring.backend.alerting.executor.RainCheckSensorData
import flock.community.office.monitoring.backend.alerting.repository.RainCheckSensorEntity
import flock.community.office.monitoring.backend.alerting.repository.RainCheckSensorMapper
import flock.community.office.monitoring.backend.alerting.repository.RainCheckSensorRepository
import flock.community.office.monitoring.utils.logging.loggerFor
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

class RainCheckSensorDataException(message: String, cause: Throwable) : RuntimeException(message, cause)

@Service
class RainCheckSensorDataService(
    private val repository: RainCheckSensorRepository,
    private val mapper: RainCheckSensorMapper
) {
    private val log = loggerFor<AlertStateService>()

    fun getDataForRule(ruleId: RuleId): RainCheckSensorData =
        try {
            val rainCheckSensorEntity =
                repository.findByRuleId(ruleId.value)
                    ?: run {
                        val event = createRainCheckSensorEntity(ruleId)
                        repository.save(
                            event
                        )
                    }
            mapper.internalize(rainCheckSensorEntity)
        } catch (t: Throwable) {
            throw RainCheckSensorDataException("Could not get active RainCheckSensorData for ${ruleId.value}", t)
        }

    fun createRainCheckSensorEntity(ruleId: RuleId): RainCheckSensorEntity = RainCheckSensorEntity(
        id = UUID.randomUUID().toString(),
        ruleId = ruleId.value,
        openedContactSensors = emptySet(),// Change me for testing:  setOf("always-open"),
        rainForecast = null,
        lastStateChange = Instant.EPOCH
    )

    // Probably want this to be atomic or something to deal with race conditions?
    fun update(rainCheckSensorData: RainCheckSensorData): RainCheckSensorData = try {
        val rainCheckSensorEntity = mapper.externalize(rainCheckSensorData)
        val savedEntity = repository.save(rainCheckSensorEntity)
        mapper.internalize(savedEntity)
    } catch (t: Throwable) {
        throw RainCheckSensorDataException("Could not update RainCheckSensorData for ${rainCheckSensorData.ruleId.value}", t)
    }
}
