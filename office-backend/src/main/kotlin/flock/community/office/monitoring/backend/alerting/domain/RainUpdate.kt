package flock.community.office.monitoring.backend.alerting.domain

import flock.community.office.monitoring.backend.weather.domain.WeatherPrediction

// TODO create sensible RainUpdate model
data class RainUpdate(
    val rain: String,
    val prediction: WeatherPrediction
)
