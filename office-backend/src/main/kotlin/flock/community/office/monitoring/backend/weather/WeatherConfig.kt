package flock.community.office.monitoring.backend.weather

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WeatherConfig {

    @Bean("OpenWeatherApiWebClient")
    fun weatherWebClient(): WebClient {
        return WebClient.builder()
            .baseUrl("https://api.openweathermap.org")
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build()
    }
}
