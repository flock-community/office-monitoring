package flock.community.office.monitoring.backend.weather.domain

import com.fasterxml.jackson.annotation.JsonProperty

data class WeatherForecastDto(
    val lat: Double,
    val lon: Double,
    val timezone: String,
    @JsonProperty("timezone_offset")
    val timezoneOffSet: Long,

    val current: CurrentWeather,
    val minutely: List<MinutelyForecastDto> = emptyList(),
    val hourly: List<HourlyForecastDto> = emptyList(),
)

data class CurrentWeather(
    val dt: Long,
    val rain: HourlyRainDto?,
    val weather: List<WeatherDto> = emptyList()
)

data class HourlyForecastDto(
    val dt: Long,
    @JsonProperty("pop")
    val probabilityOfPrecipitation: Double,
    val rain: HourlyRainDto?,
    val temp: Double,
    val pressure: Int, // in hPa
    val humidity: Int, // 1 - 100 %
    val weather: List<WeatherDto> = emptyList()

)

data class HourlyRainDto(
    @JsonProperty("1h")
    val lastHour: Double
)

data class MinutelyForecastDto(
    val dt: Long,
    val precipitation: Double
)

data class WeatherDto(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)
