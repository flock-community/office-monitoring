package flock.community.office.monitoring.backend.alerting.domain

import flock.community.office.monitoring.backend.weather.domain.WeatherForecast

// TODO create sensible RainUpdate model
data class RainUpdate(
    val rain: String,
    val forecast: WeatherForecast
)
