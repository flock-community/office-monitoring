package flock.community.office.monitoring.backend.alerting.domain

import java.time.Instant

class AlertStateId(val value: String)

data class AlertState(
    val id: AlertStateId,
    val ruleId: RuleId,
    val active: Boolean,
    val lastStateChange: Instant,
    val sentAlerts : List<SentAlert>,
)

data class SentAlert(
    val alertId: AlertId,
    val dateTime: Instant
)
