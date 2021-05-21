package flock.community.office.monitoring.backend.domain.gcp

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.google.protobuf.ByteString
import com.google.pubsub.v1.PubsubMessage
import flock.community.office.monitoring.backend.configuration.devicesMappingConfigurations
import flock.community.office.monitoring.queue.message.EventQueueMessage
import flock.community.office.monitoring.utils.logging.loggerFor
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate
import org.springframework.stereotype.Service

data class SwitchDevice( val deviceId: String, val message: OnOffState)
data class OnOffState(val state: String)

@Service
internal class CommandPublisher(
    private val pubSubTemplate: PubSubTemplate,
    private val pubSubConfig: PubSubConfig,
    private val objectMapper: ObjectMapper
) {
    private val logger = loggerFor<CommandPublisher>()

    init {
        val module = JavaTimeModule()
        objectMapper.registerModule(module)
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    fun on(deviceId: String) {

        // message should be like https://www.zigbee2mqtt.io/devices/SP_120.html
        val sensorId = deviceIdToSensorId(deviceId)
        sendDeviceState( "ON", sensorId)
    }

    fun off(deviceId: String) {
        // message should be like https://www.zigbee2mqtt.io/devices/SP_120.html
        val sensorId = deviceIdToSensorId(deviceId)
        sendDeviceState("OFF", sensorId)
    }

    private fun sendDeviceState(state: String, sensorId: String) {
        val topic = "$sensorId/set"
        val data = "{\"state\":\"$state\"}"
        val message = EventQueueMessage(topic, data)
        val messageObject = objectMapper.writeValueAsString(message)
        val pubsubMessage = PubsubMessage.newBuilder().setData(ByteString.copyFromUtf8(messageObject)).build()
        pubSubTemplate.publish(pubSubConfig.commandTopicName, pubsubMessage)
        logger.info("Changed state for device $topic to $state")
    }


    fun deviceIdToSensorId(deviceId: String): String{
        return devicesMappingConfigurations.entries
            .first { it.value.deviceId == deviceId }
            .key
    }
}