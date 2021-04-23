package flock.community.office.monitoring.backend.domain.gcp

import com.google.protobuf.ByteString
import com.google.pubsub.v1.PubsubMessage
import flock.community.office.monitoring.utils.logging.loggerFor
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
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
        val data = ByteString.copyFromUtf8("{\"deviceId\":$deviceId,\"state\":\"ON\"}")
        val pubsubMessage = PubsubMessage.newBuilder().setData(data).build()
        pubSubTemplate.publish(pubSubConfig.commandTopicName, pubsubMessage)
        logger.info("Changed state for device $deviceId to on")
    }
}