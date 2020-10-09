package flock.community.office.monitoring.mqtttoqueueconnector.mqtt

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import flock.community.office.monitoring.mqtttoqueueconnector.loggable.Loggable.Companion.logger
import flock.community.office.monitoring.mqtttoqueueconnector.queue.Publisher
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.io.Serializable
import java.time.Instant

@Service
class MqttClient(
        flockMqttCallback: FlockMqttCallback,
        mqttSettings: MQTTSettings
) {

    init {
        MqttClient(mqttSettings.endpoint, "mqtt-to-queue-connector").apply {
            connect()
            setCallback(flockMqttCallback)
            subscribe(mqttSettings.topicFilter)
        }
    }
}

@Service
class FlockMqttCallback(
        val publishers: List<Publisher>,
        val objectMapper: ObjectMapper
) : MqttCallback {

    override fun connectionLost(cause: Throwable) {
        logger.error("Lost connection to MQTT broker", cause)
        // TODO: Find out if it automatically reconnects
    }

    override fun messageArrived(topic: String, message: MqttMessage) = PublishMessage(topic, message.asString())
            .also { logger.trace("Received message: $it from MQTT broker, forwarding to publishers") }
            .let{ objectMapper.writeValueAsString(it) }
            .run { publishers.forEach { it.publish(this) } }

    override fun deliveryComplete(token: IMqttDeliveryToken) {
        logger.info("Message delivery complete")
    }

    fun MqttMessage.asString() = String(this.payload)
}

data class PublishMessage(
        val topic: String,
        val message: String,
        val received: Instant = Instant.now()
) : Serializable

@Component
data class MQTTSettings(
        @Value("\${mqqt.endpoint}") val endpoint: String,
        @Value("\${mqqt.topic.filter}") val topicFilter: String
)

