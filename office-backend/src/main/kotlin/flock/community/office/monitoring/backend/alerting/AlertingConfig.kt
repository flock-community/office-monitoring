package flock.community.office.monitoring.backend.alerting

import flock.community.office.monitoring.backend.alerting.domain.Alert
import flock.community.office.monitoring.backend.alerting.domain.AlertConfig
import flock.community.office.monitoring.backend.alerting.domain.AlertId
import flock.community.office.monitoring.backend.alerting.domain.toAlertId
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
            .baseUrl(alertingConfigurationProperties.signalAlertApi.host)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.AUTHORIZATION, alertingConfigurationProperties.signalAlertApi.token)
            .build()
    }

    @Bean("alerts")
    fun alerts(alertingConfigurationProperties: AlertingConfigurationProperties): Map<AlertId, Alert> =
        alertingConfigurationProperties.rules.flatMap {
            it.alerts.map { e ->
                val alertId = e.toAlertId(it)
                alertId to e.value.internalize(alertId)
            }
        }.toMap()

    private fun AlertConfig.internalize(
        alertId: AlertId
    ): Alert {
        return Alert(
            alertId = alertId,
            timeToDeadline = timeToDeadline,
            time = time,
            message = message,
            channel = channel
        )
    }


}
