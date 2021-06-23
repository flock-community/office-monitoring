package flock.community.office.monitoring.backend.alerting

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class AlertingConfig {

    @Bean("SignalAlertWebClient")
    fun signalAlertWebClient(alertingConfigurationProperties: AlertingConfigurationProperties): WebClient {
        return WebClient.builder()
            .baseUrl("https://signal-appi-s47ukfzmyq-ez.a.run.app")
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.AUTHORIZATION, alertingConfigurationProperties.signalAlertApi.token)
            .build()
    }
}
