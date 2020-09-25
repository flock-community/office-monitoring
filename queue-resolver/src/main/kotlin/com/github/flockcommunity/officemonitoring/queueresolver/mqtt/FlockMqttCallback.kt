package com.github.flockcommunity.officemonitoring.queueresolver.mqtt

import com.github.flockcommunity.officemonitoring.queueresolver.mapping.SensorEventMapper
import com.github.flockcommunity.officemonitoring.queueresolver.repository.SensorEventRepository
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.internal.wire.MqttReceivedMessage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class FlockMqttCallback(
        val sensorMapper: SensorEventMapper,
        val repository: SensorEventRepository
) : MqttCallback {

    private val logger = LoggerFactory.getLogger(this::class.java)

    val sensorMap = mapOf(
            "zigbee2mqtt/0x00158d0004852389" to SensorInfo("Sensor 1", SensorType.DOOR),
            "zigbee2mqtt/0x00158d000578385c" to SensorInfo("Sensor 2", SensorType.DOOR)
    )

    override fun connectionLost(throwable: Throwable?) {
        logger.error("Connection lost", throwable)
    }

    override fun messageArrived(sender: String, message: MqttMessage) = when (message) {
        is MqttReceivedMessage -> handleReceivedMessage(sender, message)
        else -> println("received unhandled message")
    }

    private fun handleReceivedMessage(sender: String, message: MqttReceivedMessage) {
        sensorMap[sender]?.let { sensorInfo ->
            val event = sensorMapper.map(sensorInfo, String(message.payload))
            repository.save(event)
            logger.info("Received event: $event")
        } ?: logger.warn("Handling unregistered event")
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        logger.info("Delivery complete: $token")
    }
}

class SensorInfo(
        val name: String,
        val type: SensorType
)

enum class SensorType {
    DOOR
}



