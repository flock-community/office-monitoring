package flock.community.office.monitoring.backend.domain.gcp

import com.fasterxml.jackson.databind.ObjectMapper
import flock.community.office.monitoring.backend.domain.service.DeviceStateSaveService
import flock.community.office.monitoring.queue.message.DeviceStateEventQueueMessage
import flock.community.office.monitoring.utils.logging.loggerFor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate
import org.springframework.cloud.gcp.pubsub.support.converter.JacksonPubSubMessageConverter
import org.springframework.cloud.gcp.pubsub.support.converter.PubSubMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service


@Service
class EventTopicSubscriber(
    pubSubTemplate: PubSubTemplate,
    private val deviceStateSaveService: DeviceStateSaveService
) {

    private val logger = loggerFor<EventTopicSubscriber>()

    init {
        pubSubTemplate.subscribeAndConvert("projects/flock-office-290609/subscriptions/office-backend", { message ->

            logger.debug("Received SensorEventQueueMessage: ${message.payload}")
            GlobalScope.launch { deviceStateSaveService.saveSensorEventQueueMessage(message.payload)}

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
