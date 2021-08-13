package flock.community.office.monitoring.backend.weather.controller

import flock.community.office.monitoring.backend.weather.WeatherEventBus
import flock.community.office.monitoring.backend.weather.domain.WeatherForecastDto
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestBody

@RestController
@RequestMapping(path = ["/admin/weather"], produces = [MediaType.APPLICATION_JSON_VALUE])
class WeatherForecastMockController(
  private val  weatherEventBus: WeatherEventBus
){

    @PostMapping
    suspend fun pushMockData(
        @RequestBody weatherForecast: WeatherForecastDto
    ){
        weatherEventBus.publish(weatherForecast)
    }



}
