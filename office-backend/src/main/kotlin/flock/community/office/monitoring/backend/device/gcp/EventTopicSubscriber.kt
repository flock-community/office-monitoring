package flock.community.office.monitoring.backend.device.gcp

import com.fasterxml.jackson.databind.ObjectMapper
import flock.community.office.monitoring.backend.device.service.DeviceStateSaveService
import flock.community.office.monitoring.queue.message.DeviceStateEventQueueMessage
import flock.community.office.monitoring.utils.logging.loggerFor
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate
import org.springframework.cloud.gcp.pubsub.support.converter.JacksonPubSubMessageConverter
import org.springframework.cloud.gcp.pubsub.support.converter.PubSubMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service

@ConstructorBinding
@ConfigurationProperties("pubsub")
data class PubSubConfig(
    val subscriptionName: String
)

@Service
class EventTopicSubscriber(
    pubSubTemplate: PubSubTemplate,
    pubSubConfig: PubSubConfig,
    private val deviceStateSaveService: DeviceStateSaveService
) {

    private val logger = loggerFor<EventTopicSubscriber>()

    init {
        logger.info("Subscribing to ${pubSubConfig.subscriptionName}")
        pubSubTemplate.subscribeAndConvert(pubSubConfig.subscriptionName, { message ->
            try {
                logger.debug("Received SensorEventQueueMessage: ${message.pubsubMessage.messageId} - ${message.payload}")
                deviceStateSaveService.saveSensorEventQueueMessage(message.payload)

                message.ack().addCallback(
                    { logger.debug("SensorEventQueueMessage acked : ${message.pubsubMessage.messageId} - ${message.payload}") },
                    { logger.warn("Message ack failed ${message.pubsubMessage.messageId} - ${message.payload}") }
                )
            } catch (e: Exception) {
                logger.warn("Something went wrong processing message ${message.pubsubMessage.messageId} ${message.payload}")
                message.nack().addCallback(
                    { logger.debug("SensorEventQueueMessage nacked ${message.pubsubMessage.messageId} - ${message.payload}") },
                    { logger.warn("Message nack failed ${message.pubsubMessage.messageId} - ${message.payload}") }
                )
            }
        }, DeviceStateEventQueueMessage::class.java)
    }
}

@Configuration
class EventMapperConfiguration(
    val objectMapper: ObjectMapper
) {
    @Bean
    fun pubSubMessageConverter(): PubSubMessageConverter? {
        return JacksonPubSubMessageConverter(objectMapper)
    }
}
