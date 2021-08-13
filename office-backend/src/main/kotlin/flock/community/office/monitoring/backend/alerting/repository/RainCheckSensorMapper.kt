package flock.community.office.monitoring.backend.alerting.repository

import flock.community.office.monitoring.backend.alerting.domain.HourlyRainForecast
import flock.community.office.monitoring.backend.alerting.domain.RuleId
import flock.community.office.monitoring.backend.alerting.domain.Weather
import flock.community.office.monitoring.backend.alerting.executor.RainCheckSensorData
import flock.community.office.monitoring.backend.alerting.executor.RainCheckSensorDataId
import org.springframework.stereotype.Service

@Service
class RainCheckSensorMapper {
    fun internalize(e: RainCheckSensorEntity) = RainCheckSensorData(
        id = RainCheckSensorDataId(e.id),
        ruleId = RuleId(e.ruleId),
        openedContactSensors = e.openedContactSensors,
        rainForecast = e.rainForecast.internalize(),
        lastStateChange = e.lastStateChange

    )

    fun externalize(i: RainCheckSensorData) = RainCheckSensorEntity(
        id = i.id.value,
        ruleId = i.ruleId.value,
        openedContactSensors = i.openedContactSensors,
        rainForecast = i.rainForecast.externalize(),
        lastStateChange = i.lastStateChange
    )

    private fun HourlyRainForecastDto?.internalize(): HourlyRainForecast? = this?.let {
        HourlyRainForecast(
            dateTime = it.dateTime,
            precipitationChance = it.precipitationChance,
            precipitationVolume = it.precipitationVolume,
            description = it.description.internalizeWeather()
        )
    }

    private fun HourlyRainForecast?.externalize(): HourlyRainForecastDto? = this?.let {
        HourlyRainForecastDto(
            dateTime = it.dateTime,
            precipitationChance = it.precipitationChance,
            precipitationVolume = it.precipitationVolume,
            description = it.description.externalizeWeather()
        )
    }

    private fun List<WeatherDto>.internalizeWeather(): List<Weather> = this.map {
        Weather(
            id = it.id,
            groupName = it.groupName,
            description = it.description,
            icon = it.icon
        )
    }

    private fun List<Weather>.externalizeWeather(): List<WeatherDto> = this.map {
        WeatherDto(
            id = it.id,
            groupName = it.groupName,
            description = it.description,
            icon = it.icon
        )
    }
}
