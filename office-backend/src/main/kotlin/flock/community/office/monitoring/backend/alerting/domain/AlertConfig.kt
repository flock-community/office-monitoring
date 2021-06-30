package flock.community.office.monitoring.backend.alerting.domain

import java.time.Duration

@JvmInline
value class AlertId(val value: String)

data class Alert(
    val alertId: AlertId,
    val timeToDeadline: Duration,
    val message: String,
    val channel: AlertChannel
)

data class AlertConfig(
    val timeToDeadline: Duration,
    val message: String,
    val channel: AlertChannel
);

enum class AlertChannel {
    SIGNAL
}


fun Map.Entry<String, AlertConfig>.toAlertId(
    rule: Rule
) = this.toAlertId(rule.id)


fun Map.Entry<String, AlertConfig>.toAlertId(
    ruleId: RuleId
) = AlertId("${ruleId.value}--${this.key}")
