package flock.community.office.monitoring.backend.alerting.domain

import java.time.Duration

class RuleId(val value: String)

data class Rule(
    val id: RuleId,
    val type: RuleType,
    val name: String,
    val cancelMessage: CancelAlert,
    val alerts: Map<String, AlertConfig> = emptyMap(),
    val alertingWindow: Duration,
    val deviceIds: List<String>,
    val metaData: Map<String, String>
)


data class CancelAlert(
    val message: String,
    val channel: AlertChannel
)

enum class RuleType{
    RAIN_CHECK_CONTACT_SENSOR
}

