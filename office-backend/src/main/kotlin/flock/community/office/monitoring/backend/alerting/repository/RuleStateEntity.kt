package flock.community.office.monitoring.backend.alerting.repository

import org.springframework.cloud.gcp.data.datastore.core.mapping.Entity
import org.springframework.data.annotation.Id
import java.time.Instant

@Entity(name = "ruleState")
data class RuleStateEntity(
    @Id
    val id: String,
    val ruleId: String,
    val active: Boolean,
    val openedContactSensors: Set<String>,
    val rainForecast: HourlyRainForecastDto?,
    val lastStateChange: Instant,
    val sentAlerts : List<SentAlertDto>,
)

@Entity
data class HourlyRainForecastDto(
    val dateTime: Instant,
    val precipitationChance: Double,
    val precipitationVolume: Double, // volume, mm
    val description: List<WeatherDto>
)

@Entity
data class WeatherDto(
    val id: Int,
    val groupName: String,
    val description: String,
    val icon: String
)

@Entity
data class SentAlertDto(
    val openedContactSensors: List<String>, // Fixme: temp value to record last open/close state used for sending events
    val alertId: String,
    val dateTime: Instant
)
