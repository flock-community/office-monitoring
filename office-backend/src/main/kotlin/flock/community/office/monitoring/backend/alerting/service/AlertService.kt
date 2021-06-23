package flock.community.office.monitoring.backend.alerting.service

import flock.community.office.monitoring.backend.alerting.client.SignalAlertClient
import flock.community.office.monitoring.backend.alerting.domain.Alert
import org.springframework.stereotype.Service

@Service
class AlertService(
    val signalAlertClient: SignalAlertClient
) {
    suspend fun send(alert: Alert, properties: Map<String, String>): Boolean {
//        println("Sending alert: ${alert.message} (channel ${alert.channel})")

        val interpretedMessage = interpolate(alert.message, properties)

        signalAlertClient.sendMessage(interpretedMessage)
        return true
    }

    /*private*/ fun interpolate(message: String, properties: Map<String, String>): String {
        return message.replace(Regex("\\{\\{(.*?)\\}\\}")) { it ->
            properties.getOrDefault(it.groups[1]?.value, it.value)
        }
    }
}

