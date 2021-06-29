package flock.community.office.monitoring.backend.alerting.domain

import java.time.Instant


data class RainForecast(
    val lat: Double,
    val lon: Double,
    val dateTime: Instant,
    val currentForecast: HourlyRainForecast,
//    val minutelyForecast: List<MinutelyForecast> = emptyList(),
    val hourlyForecast: List<HourlyRainForecast> = emptyList(),
)

data class MinutelyForecast(
    val dateTime: Instant,
    val precipitationVolume: Double // percentage, 0 - 100
)

data class HourlyRainForecast(
    val dateTime: Instant,
    val precipitationChance: Double,
    val precipitationVolume: Double, // volume, mm
    val description: List<Weather>
)

data class Weather(
    val id: Int,
    val groupName: String,
    val description: String,
    val icon: String
)

fun HourlyRainForecast?.containsRainForecast(minimalVolume: Double, minimalProbability: Double): Boolean =
    this?.let {
        it.description.any { d -> d.groupName == "Rain" }
                || it.precipitationVolume >= minimalVolume
                || it.precipitationChance > minimalProbability
    }
        ?: false
