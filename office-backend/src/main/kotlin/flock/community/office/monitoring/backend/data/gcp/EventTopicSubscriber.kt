package flock.community.office.monitoring.backend.data.gcp

import flock.community.office.monitoring.backend.data.service.EventSaveService
import flock.community.office.monitoring.queue.message.SensorEventQueueMessage
import flock.community.office.monitoring.utils.logging.Loggable.Companion.logger
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate
import org.springframework.stereotype.Service

@Service
class EventTopicSubscriber(
    private val pubSubTemplate: PubSubTemplate,
    private val eventSaveService: EventSaveService
) {

    init {

        pubSubTemplate.subscribeAndConvert("", { message ->

            logger.debug("Received SensorEventQueueMessage")
            message.payload

        }, SensorEventQueueMessage::class.java )

    }
}
