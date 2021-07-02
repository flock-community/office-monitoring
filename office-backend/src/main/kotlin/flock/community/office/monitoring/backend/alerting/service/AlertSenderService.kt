package flock.community.office.monitoring.backend.alerting.service

import flock.community.office.monitoring.backend.alerting.AlertingConfigurationProperties
import flock.community.office.monitoring.backend.alerting.client.SignalAlertClient
import flock.community.office.monitoring.backend.alerting.domain.Alert
import flock.community.office.monitoring.backend.alerting.domain.AlertChannel
import flock.community.office.monitoring.backend.alerting.domain.AlertConfig
import flock.community.office.monitoring.backend.alerting.domain.CancelAlert
import flock.community.office.monitoring.backend.utils.client.garbled
import flock.community.office.monitoring.utils.logging.loggerFor
import org.springframework.stereotype.Service

@Service
class AlertSenderService(
    private val signalAlertClient: SignalAlertClient,
    private val config: AlertingConfigurationProperties
) {
    private val log = loggerFor<AlertSenderService>()

    suspend fun send(alert: CancelAlert, properties: Map<String, String>): Boolean = this.send(
        message = alert.message,
        channel = alert.channel,
        properties = properties
    )

    suspend fun send(alert: Alert, properties: Map<String, String>): Boolean = this.send(
        message = alert.message,
        channel = alert.channel,
        properties = properties
    )

    suspend fun send(alert: AlertConfig, properties: Map<String, String>): Boolean {
        return send(alert.message, alert.channel, properties)
    }

    suspend fun send(message: String, channel: AlertChannel, properties: Map<String, String>): Boolean =
        when (channel) {
            AlertChannel.SIGNAL -> sendSignalMessage(message, properties)
        }

    private suspend fun sendSignalMessage(
        message: String,
        properties: Map<String, String>
    ): Boolean {
        val interpretedMessage = interpolate(message, properties)

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
        return true
    }


    private fun logMessage(phoneNumber: String, message: String) {
        log.info(
            """
                .
                ☎️ -- ${phoneNumber.garbled()}
                ✉️ --  
                $message  
                --
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

