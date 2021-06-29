package flock.community.office.monitoring.backend.alerting.service

import flock.community.office.monitoring.backend.alerting.AlertingConfigurationProperties
import flock.community.office.monitoring.backend.alerting.client.SignalAlertClient
import flock.community.office.monitoring.backend.alerting.domain.Alert
import flock.community.office.monitoring.backend.alerting.domain.AlertConfig
import flock.community.office.monitoring.backend.utils.client.garbled
import flock.community.office.monitoring.utils.logging.loggerFor
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service

@Service
class AlertSenderService(
    private val signalAlertClient: SignalAlertClient,
    private val config: AlertingConfigurationProperties
) {

    private val log = loggerFor<AlertSenderService>()

    suspend fun send(alert: Alert, properties: Map<String, String>): Boolean = this.send(
        alert = AlertConfig(
            time = alert.time,
            timeToDeadline = alert.timeToDeadline,
            message = alert.message,
            channel = alert.channel
        ),
        properties = properties
    )


    suspend fun send(alert: AlertConfig, properties: Map<String, String>): Boolean = coroutineScope {
        val interpretedMessage = interpolate(alert.message, properties)

        val x = async {
            config.signalAlertApi.phoneNumbers.map {
                logMessage(it, interpretedMessage)
                if (config.signalAlertApi.enabled) {
                    try {
                        signalAlertClient.sendMessage(it, interpretedMessage)
                    } catch (t: Throwable) {
                        log.warn("Could not send alert to ${it.garbled()}: $interpretedMessage")
                    }
                } else {
                    log.info("Not sending alert over API (api is disabled)")
                }
            }
        }

        awaitAll(x)
        true
    }


    private fun logMessage(phoneNumber: String, message: String) {
        log.info(
            """
                .
                .
                ☎️ -- ${phoneNumber.garbled()}
                ✉️ --  $message  
                .
                .
                """.trimIndent()
        )
    }

    /*private*/ fun interpolate(message: String, properties: Map<String, String>): String {
        return message.replace(Regex("\\{\\{(.*?)\\}\\}")) {
            properties.getOrDefault(it.groups[1]?.value, it.value)
        }
    }
}

