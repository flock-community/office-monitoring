package flock.community.office.monitoring.backend.alerting.domain

import java.time.Duration


//@JvmInline
/*value*/ class RuleId(val value: String)

data class Rule(
    val id: RuleId,
    val type: RuleType,
    val name: String,
    val alerts: Map<String, AlertConfig> = emptyMap(),
    val alertingWindow: Duration,
    val deviceIds: List<String>,
    val metaData: Map<String, String>
)

enum class RuleType{
    RAIN_CHECK_CONTACT_SENSOR
}

