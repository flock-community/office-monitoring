package flock.community.office.monitoring.backend.weather.service

import flock.community.office.monitoring.backend.utils.client.HttpServerException
import flock.community.office.monitoring.backend.utils.client.httpGuard
import flock.community.office.monitoring.backend.utils.client.verifyHttpStatus
import flock.community.office.monitoring.backend.weather.domain.WeatherForecast
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange
import org.springframework.web.util.UriBuilder

@Service
class WeatherService(
    private val webClient: WebClient,
    private val config: OpenWeatherMapConfig
) {

    suspend fun getPrediction(): WeatherForecast {
        return httpGuard({ ex ->
            HttpServerException(
                "Unexpected error fetching Weather prediction: ${ex.message}",
                ex
            )
        }) {
            val uri = webClient.get()
                .uri { it.buildUrl() }
            uri
                .awaitExchange {
                    it.verifyHttpStatus()
                    it.awaitBody()
                }
//                .also { it.verifyHttpStatus() }
//                .awaitBody()
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

@ConstructorBinding
@ConfigurationProperties("weather.open-weather-map")
data class OpenWeatherMapConfig(
    val apiKey: String,
    val latitude: Double,
    val longitude: Double
)





