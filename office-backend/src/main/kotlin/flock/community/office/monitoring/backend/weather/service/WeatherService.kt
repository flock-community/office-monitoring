package flock.community.office.monitoring.backend.weather.service

import flock.community.office.monitoring.backend.weather.domain.Coord
import flock.community.office.monitoring.backend.weather.domain.WeatherPrediction
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.util.UriComponentsBuilder

@Service
class WeatherService(
    private val webClient: WebClient,
    private val openWeatherMapConfig: OpenWeatherMapConfig
) {

    private val flockOfficeCoordinates: Coord = Coord(52.09266175027509, 5.122345051397365)

    private fun buildUrl(): String =
        "?lat=${flockOfficeCoordinates.lat}&lon=${flockOfficeCoordinates.lon}&appid=${openWeatherMapConfig.apiKey}&units=metric"

    // TODO: proper error handling
    suspend fun getPrediction(): WeatherPrediction? {
        return guard {
            webClient.get()
                .uri(buildUrl())
                .retrieve()
                .awaitBody()
        }
    }

    private suspend fun <T> guard(block: suspend () -> T): T? {
        return try {
            block()
        } catch (e: Throwable) {
            // TODO: do something here
            null
        }
    }
}

@Configuration
class WeatherServiceConfig() {

    @Bean
    fun weatherWebClient(): WebClient {
        return WebClient.builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/weather")
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build()
    }
}

@ConstructorBinding
@ConfigurationProperties("open-weather-map")
data class OpenWeatherMapConfig(
    val apiKey: String
)





