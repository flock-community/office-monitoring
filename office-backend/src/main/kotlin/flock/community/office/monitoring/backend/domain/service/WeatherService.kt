package flock.community.office.monitoring.backend.domain.service

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class WeatherService (){

    private val flockOfficeCoordinates: String = "52.11528484782564,5.17333337075482" //"52.092675932902495, 5.122232208228056"

    private var webClient: WebClient? = WebClient.builder()
        .baseUrl("https://weerlive.nl/api/json-data-10min.php?")
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build()


    private fun buildUrl(apiKey: String) : String = "?key=${apiKey}&locatie=${flockOfficeCoordinates}"

    fun getPrediction(): Mono<WeatherPrediction>? {
        return webClient?.get()
            ?.uri(buildUrl("aedcad93e4"))
            ?.retrieve()
            ?.bodyToMono(WeatherPrediction::class.java)
    }

}


class WeatherPrediction {

    @JsonProperty("liveweer")
    val liveweer: List<Liveweer>? = null

}

data class Liveweer (

    @JsonProperty("plaats")
    val plaats: String = "",

    @JsonProperty("temp")
    val temp: String,

    @JsonProperty("samenv")
    val samenv: String,

    @JsonProperty("verw")
    val verw: String

)