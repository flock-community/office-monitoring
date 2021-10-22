package flock.community.office.monitoring.backend.alerting.repository

import flock.community.office.monitoring.backend.alerting.domain.RuleId
import flock.community.office.monitoring.backend.alerting.domain.ContactSensorAlertData
import flock.community.office.monitoring.backend.alerting.domain.ContactSensorAlertDataId
import org.springframework.stereotype.Service

@Service
class ContactSensorAlertingMapper {
    fun internalize(e: ContactSensorAlertingEntity) = ContactSensorAlertData(
        id = ContactSensorAlertDataId(e.id),
        ruleId = RuleId(e.ruleId),
        openedContactSensors = e.openedContactSensors,
        lastStateChange = e.lastStateChange

    )

    fun externalize(i: ContactSensorAlertData) = ContactSensorAlertingEntity(
        id = i.id.value,
        ruleId = i.ruleId.value,
        openedContactSensors = i.openedContactSensors,
        lastStateChange = i.lastStateChange
    )
}
