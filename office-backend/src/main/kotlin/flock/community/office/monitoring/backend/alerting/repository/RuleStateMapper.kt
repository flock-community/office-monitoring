package flock.community.office.monitoring.backend.alerting.repository

import flock.community.office.monitoring.backend.alerting.domain.AlertId
import flock.community.office.monitoring.backend.alerting.domain.HourlyRainForecast
import flock.community.office.monitoring.backend.alerting.domain.RuleId
import flock.community.office.monitoring.backend.alerting.domain.RuleState
import flock.community.office.monitoring.backend.alerting.domain.RuleStateId
import flock.community.office.monitoring.backend.alerting.domain.SentAlert
import flock.community.office.monitoring.backend.alerting.domain.Weather
import org.springframework.stereotype.Service

@Service
class RuleStateMapper {

    fun internalize(e: RuleStateEntity) = RuleState(
        id = RuleStateId(e.id),
        ruleId = RuleId(e.ruleId),
        active = e.active,
        openedContactSensors = e.openedContactSensors,
        rainForecast = e.rainForecast.internalize(),
        lastStateChange = e.lastStateChange,
        sentAlerts = e.sentAlerts.internalizeSentAlerts()

    )

    fun externalize(i: RuleState) = RuleStateEntity(
        id = i.id.value,
        ruleId = i.ruleId.value,
        active = i.active,
        openedContactSensors = i.openedContactSensors,
        rainForecast = i.rainForecast.externalize(),
        lastStateChange = i.lastStateChange,
        sentAlerts = i.sentAlerts.externalizeSentAlerts()

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

    private fun List<SentAlertDto>.internalizeSentAlerts(): List<SentAlert> = this.map {
        SentAlert(
            openedContactSensors = it.openedContactSensors.toSet(),
            alertId = AlertId(it.alertId),
            dateTime = it.dateTime
        )
    }

    private fun List<SentAlert>.externalizeSentAlerts(): List<SentAlertDto> = this.map {
        SentAlertDto(
            openedContactSensors = it.openedContactSensors.toList(),
            alertId = it.alertId.value,
            dateTime = it.dateTime
        )
    }
}






