package flock.community.office.monitoring.backend.domain.gcp

import com.fasterxml.jackson.databind.ObjectMapper
import flock.community.office.monitoring.backend.domain.service.EventSaveService
import flock.community.office.monitoring.queue.message.SensorEventQueueMessage
import flock.community.office.monitoring.utils.logging.Loggable.Companion.logger
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate
import org.springframework.cloud.gcp.pubsub.support.converter.JacksonPubSubMessageConverter
import org.springframework.cloud.gcp.pubsub.support.converter.PubSubMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service


@Service
class EventTopicSubscriber(
    pubSubTemplate: PubSubTemplate,
    private val eventSaveService: EventSaveService
) {

    init {
        pubSubTemplate.subscribeAndConvert("projects/flock-office-290609/subscriptions/office-backend", { message ->

            logger.debug("Received SensorEventQueueMessage: ${message.payload}")
            eventSaveService.saveSensorEventQueueMessage(message.payload)

        }, SensorEventQueueMessage::class.java)
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
