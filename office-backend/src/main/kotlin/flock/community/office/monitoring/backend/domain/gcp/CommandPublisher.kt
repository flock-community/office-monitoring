package flock.community.office.monitoring.backend.domain.gcp

import com.google.protobuf.ByteString
import com.google.pubsub.v1.PubsubMessage
import flock.community.office.monitoring.utils.logging.loggerFor
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate
import org.springframework.stereotype.Service

@Service
internal class CommandPublisher(
    private val pubSubTemplate: PubSubTemplate,
    private val pubSubConfig: PubSubConfig
) {
    private val logger = loggerFor<CommandPublisher>()

    fun on(deviceId: String) {
        // message should be like https://www.zigbee2mqtt.io/devices/SP_120.html
        val data = ByteString.copyFromUtf8("{\"deviceId\":$deviceId,\"message\":{\"state\":\"ON\"}}")
        val pubsubMessage = PubsubMessage.newBuilder().setData(data).build()
        pubSubTemplate.publish(pubSubConfig.commandTopicName, pubsubMessage)
        logger.info("Changed state for device $deviceId to on")
    }

    fun off(deviceId: String) {
        // message should be like https://www.zigbee2mqtt.io/devices/SP_120.html
        val data = ByteString.copyFromUtf8("{\"deviceId\":$deviceId,\"message\":{\"state\":\"OFF\"}}")
        val pubsubMessage = PubsubMessage.newBuilder().setData(data).build()
        pubSubTemplate.publish(pubSubConfig.commandTopicName, pubsubMessage)
        logger.info("Changed state for device $deviceId to off")
    }

    fun state(deviceId: String) {
        // message should be like https://www.zigbee2mqtt.io/devices/SP_120.html
        val data = ByteString.copyFromUtf8("{\"deviceId\":$deviceId,\"message\":{\"state\":\"\"}}")
        val pubsubMessage = PubsubMessage.newBuilder().setData(data).build()
        pubSubTemplate.publish(pubSubConfig.commandTopicName, pubsubMessage)
        logger.info("Get state for device $deviceId")
    }
}