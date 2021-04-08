package flock.community.office.monitoring.backend.domain.service

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class WeatherService() {

    private val flockOfficeCoordinates: Pair<String, String> = Pair("52.09266175027509", "5.122345051397365")

    private var webClient: WebClient = WebClient.builder()
        .baseUrl("https://api.openweathermap.org/data/2.5/weather")
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build()


    private fun buildUrl(coordinates: Pair<String, String>, apiKey: String): String =
        "?lat=${coordinates.first}&lon=${coordinates.second}&appid=${apiKey}&units=metric"

    fun getPrediction(): Mono<WeatherPrediction> {
        return webClient.get()
            .uri(buildUrl(flockOfficeCoordinates, ""))
            .retrieve()
            .bodyToMono(WeatherPrediction::class.java)
    }

}

data class WeatherPrediction(
    val base: String?,
    val clouds: Clouds?,
    val cod: Int?,
    val coord: Coord,
    val dt: Int,
    val id: Int,
    val main: Main,
    val name: String,
    val sys: Sys,
    val timezone: Int,
    val visibility: Int,
    val weather: List<Weather>,
    val wind: Wind?
)

data class Clouds(
    val all: Int
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
    val description: String,
    val icon: String,
    val id: Int,
    val main: String
)

data class Wind(
    val deg: Int,
    val gust: Double,
    val speed: Double
)





