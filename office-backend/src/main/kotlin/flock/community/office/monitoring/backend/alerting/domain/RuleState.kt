package flock.community.office.monitoring.backend.alerting.domain

import java.time.Instant

/*@JvmInline*/
/*value*/ class RuleStateId(val value: String)

data class RuleState(
    val id: RuleStateId,
    val ruleId: RuleId,
    val active: Boolean,
    val openedContactSensors: Set<String>, //deviceIds
    val rainForecast: HourlyRainForecast?,
    val lastStateChange: Instant,
    val sentAlerts : List<SentAlert>,
)

data class SentAlert(
    val openedContactSensors: Set<String>, // Fixme: temp value to record last open/close state used for sending events
    val alertId: AlertId,
    val dateTime: Instant
)
