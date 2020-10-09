package flock.community.office.monitoring.mqtttoqueueconnector.queue.google

import flock.community.office.monitoring.mqtttoqueueconnector.loggable.Loggable
import flock.community.office.monitoring.mqtttoqueueconnector.loggable.Loggable.Companion.logger
import flock.community.office.monitoring.mqtttoqueueconnector.queue.Publisher
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate
import org.springframework.stereotype.Service

@Service
class GCPQueuePublisher(
        val pubSubTemplate: PubSubTemplate,
        @Value("\${office.event.queue.name}") val sensorEventQueueName: String
) : Publisher, Loggable {

    override fun publish(message: String) {
        pubSubTemplate.publish(sensorEventQueueName, message).apply {

            val successCallback: (String?) -> Unit = { logger.debug("Published message: $message to GCP topic: $it") }
            val errorCallback: (Throwable) -> Unit = { logger.error("publishing message: $message to GCP failed: $it") }

            addCallback(successCallback, errorCallback)
        }
    }
}