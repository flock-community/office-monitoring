package flock.community.office.monitoring.backend.alerting.service

import flock.community.office.monitoring.backend.alerting.domain.HourlyRainForecast
import flock.community.office.monitoring.backend.alerting.domain.RainForecast
import flock.community.office.monitoring.backend.alerting.domain.Weather
import flock.community.office.monitoring.backend.weather.domain.WeatherDto
import flock.community.office.monitoring.backend.weather.domain.WeatherForecastDto
import java.time.Instant

fun WeatherForecastDto.toRainUpdate() = RainForecast(
    lat = lat,
    lon = lon,
    dateTime = Instant.ofEpochSecond(current.dt),
    currentForecast = HourlyRainForecast(
        dateTime = Instant.ofEpochSecond(current.dt),
        precipitationChance = 0.0,
        precipitationVolume = current.rain?.lastHour ?: 0.0,
        description = current.weather.map { it.toWeather() }
    ),
    hourlyForecast = hourly.map {
        HourlyRainForecast(
            dateTime = Instant.ofEpochSecond(it.dt),
            precipitationChance = it.probabilityOfPrecipitation,
            precipitationVolume = it.rain?.lastHour ?: 0.0,
            description = it.weather.map { w -> w.toWeather() }
        )
    }.sortedBy { it.dateTime },
)

private fun WeatherDto.toWeather() = Weather(
    id = id,
    groupName = main,
    description = description,
    icon = icon
)
