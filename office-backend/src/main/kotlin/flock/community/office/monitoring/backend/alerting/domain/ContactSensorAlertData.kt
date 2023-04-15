package flock.community.office.monitoring.backend.alerting.domain

import java.time.Instant

@JvmInline
value class ContactSensorAlertDataId(val value: String)
data class ContactSensorAlertData(
    val id: ContactSensorAlertDataId,
    val ruleId: RuleId,
    val openedContactSensors: Set<String>,
    val lastStateChange: Instant,
)
