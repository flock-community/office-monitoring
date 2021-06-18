package flock.community.office.monitoring.backend.weather.domain

import com.fasterxml.jackson.annotation.JsonProperty

data class WeatherForecast(
    val lat: Double,
    val lon: Double,
    val timezone: String,
    @JsonProperty("timezone_offset")
    val timezoneOffSet: Long,

    val minutely: List<MinutelyForecastDto> = emptyList(),
    val hourly: List<HourlyForecastDto> = emptyList(),
)

data class HourlyForecastDto(
    val dt: Long,
    @JsonProperty("pop")
    val probabilityOfPrecipitation: Int,
    val rain: HourlyRainDto?,
    val temp: Double,
    val pressure: Int, // in hPa
    val humidity: Int, // 1 - 100 %
    val weather: List<Weather> = emptyList()

)

data class HourlyRainDto(
    @JsonProperty("1h")
    val lastHour: Int
)

data class MinutelyForecastDto(
    val dt: Long,
    val precipitation: Double
)

data class Coord(
    val lat: Double,
    val lon: Double
)

data class Main(
    val feels_like: Double,
    val humidity: Int,
    val pressure: Int,
    val temp: Double,
    val temp_max: Double,
    val temp_min: Double
)

data class Sys(
    val country: String,
    val id: Int,
    val sunrise: Int,
    val sunset: Int,
    val type: Int
)

data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)
