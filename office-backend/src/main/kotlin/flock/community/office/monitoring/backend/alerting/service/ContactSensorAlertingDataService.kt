package flock.community.office.monitoring.backend.alerting.service

import flock.community.office.monitoring.backend.alerting.domain.RuleId
import flock.community.office.monitoring.backend.alerting.domain.ContactSensorAlertData
import flock.community.office.monitoring.backend.alerting.repository.ContactSensorAlertingEntity
import flock.community.office.monitoring.backend.alerting.repository.ContactSensorAlertingMapper
import flock.community.office.monitoring.backend.alerting.repository.ContactSensorAlertingRepository
import flock.community.office.monitoring.utils.logging.loggerFor
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

class ContactSensorAlertingDataException(message: String, cause: Throwable) : RuntimeException(message, cause)

@Service
class ContactSensorAlertingDataService(
    private val repository: ContactSensorAlertingRepository,
    private val mapper: ContactSensorAlertingMapper
) {
    private val log = loggerFor<AlertStateService>()

    fun getDataForRule(ruleId: RuleId): ContactSensorAlertData =
        try {
            val rainCheckSensorEntity =
                repository.findByRuleId(ruleId.value)
                    ?: run {
                        val event = createContactSensorAlertingEntity(ruleId)
                        repository.save(event)
                    }
            mapper.internalize(rainCheckSensorEntity)
        } catch (t: Throwable) {
            throw RainCheckSensorDataException("Could not get active RainCheckSensorData for ${ruleId.value}", t)
        }

    fun createContactSensorAlertingEntity(ruleId: RuleId): ContactSensorAlertingEntity = ContactSensorAlertingEntity(
        id = UUID.randomUUID().toString(),
        ruleId = ruleId.value,
        openedContactSensors = emptySet(),// Change me for testing:  setOf("always-open"),
        lastStateChange = Instant.EPOCH
    )

    // Probably want this to be atomic or something to deal with race conditions?
    fun update(contactSensorAlertData: ContactSensorAlertData): ContactSensorAlertData = try {
        val rainCheckSensorEntity = mapper.externalize(contactSensorAlertData)
        val savedEntity = repository.save(rainCheckSensorEntity)
        mapper.internalize(savedEntity)
    } catch (t: Throwable) {
        throw RainCheckSensorDataException("Could not update ContactSensorAlertingData for ${contactSensorAlertData.ruleId.value}", t)
    }
}
