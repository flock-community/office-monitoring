package flock.community.office.monitoring.backend.weather.service

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("weather.open-weather-map")
data class OpenWeatherMapConfig(
    val apiKey: String,
    val latitude: Double,
    val longitude: Double
)
