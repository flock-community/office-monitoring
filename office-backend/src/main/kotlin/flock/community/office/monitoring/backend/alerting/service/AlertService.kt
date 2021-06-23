package flock.community.office.monitoring.backend.alerting.service

import flock.community.office.monitoring.backend.alerting.AlertingConfigurationProperties
import flock.community.office.monitoring.backend.alerting.client.SignalAlertClient
import flock.community.office.monitoring.backend.alerting.domain.Alert
import flock.community.office.monitoring.backend.utils.client.garbled
import flock.community.office.monitoring.utils.logging.loggerFor
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service

@Service
class AlertService(
    private val signalAlertClient: SignalAlertClient,
    private val config: AlertingConfigurationProperties
) {

    private val log = loggerFor<AlertService>()

    suspend fun send(alert: Alert, properties: Map<String, String>): Boolean  = coroutineScope {
//        println("Sending alert: ${alert.message} (channel ${alert.channel})")

        val interpretedMessage = interpolate(alert.message, properties)


        val x = async{
            config.signalAlertApi.phoneNumbers.map {
                try {
                    signalAlertClient.sendMessage(it, interpretedMessage)
                } catch (t: Throwable) {
                    log.warn("Could not send alert to ${it.garbled()}: $interpretedMessage")
                }
            }
        }

        awaitAll(x)
        true
    }

    /*private*/ fun interpolate(message: String, properties: Map<String, String>): String {
        return message.replace(Regex("\\{\\{(.*?)\\}\\}")) { it ->
            properties.getOrDefault(it.groups[1]?.value, it.value)
        }
    }
}

