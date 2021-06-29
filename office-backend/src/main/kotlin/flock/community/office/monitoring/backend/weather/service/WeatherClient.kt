package flock.community.office.monitoring.backend.weather.service

import flock.community.office.monitoring.backend.utils.client.HttpServerException
import flock.community.office.monitoring.backend.utils.client.guard
import flock.community.office.monitoring.backend.utils.client.verifyHttpStatus
import flock.community.office.monitoring.backend.weather.domain.WeatherForecastDto
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange
import org.springframework.web.util.UriBuilder

@Component
class WeatherClient(
    @Qualifier("OpenWeatherApiWebClient") private val webClient: WebClient,
    private val config: OpenWeatherMapConfig
) {

    suspend fun getForecast(): WeatherForecastDto {
        return guard({ ex ->
            HttpServerException("Unexpected error fetching Weather prediction: ${ex.message}", ex)
        }) {
            webClient.get()
                .uri { it.buildUrl() }
                .awaitExchange {
                    it.verifyHttpStatus()
                    it.awaitBody()
                }
        }
    }

    private fun UriBuilder.buildUrl() = path("/data/2.5/onecall")
        .queryParam("lat", config.latitude)
        .queryParam("lon", config.longitude)
        .queryParam("appid", config.apiKey)
        .queryParam("exclude", "daily,alerts")
        .queryParam("units", "metric")
        .build()
}





