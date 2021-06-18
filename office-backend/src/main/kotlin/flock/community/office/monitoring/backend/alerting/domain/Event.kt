package flock.community.office.monitoring.backend.alerting.domain

import flock.community.office.monitoring.backend.weather.domain.WeatherForecast
import java.time.Instant

@JvmInline
value class EventId(val value: String)

data class Event(
    val id: EventId,
    val ruleId: RuleId,
    val state: EventState,
    val openedContactSensors: Set<String>, //deviceIds
    val rainForecast: WeatherForecast,
    val lastStateChange: Instant,
    val sentAlerts : List<SentAlert>
)

data class SentAlert(
    val alert: Alert,
    val dateTime: Instant
)


enum class EventState {
    ACTIVE,
    IDLE
}
