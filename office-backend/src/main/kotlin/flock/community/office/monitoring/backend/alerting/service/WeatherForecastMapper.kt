package flock.community.office.monitoring.backend.alerting.executor

import flock.community.office.monitoring.backend.alerting.domain.HourlyRainForecast
import flock.community.office.monitoring.backend.alerting.domain.RainForecast
import flock.community.office.monitoring.backend.alerting.domain.Weather
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
        description = current.weather.map { w ->
            Weather(
                id = w.id,
                groupName = w.main,
                description = w.description,
                icon = w.icon
            )
        }

    ),
    hourlyForecast = hourly.map {
        HourlyRainForecast(
            dateTime = Instant.ofEpochSecond(it.dt),
            precipitationChance = it.probabilityOfPrecipitation,
            precipitationVolume = it.rain?.lastHour ?: 0.0,
            description = it.weather.map { w ->
                Weather(
                    id = w.id,
                    groupName = w.main,
                    description = w.description,
                    icon = w.icon
                )
            }

        )
    }
        .sortedBy { it.dateTime },
)
