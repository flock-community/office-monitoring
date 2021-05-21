package flock.community.office.monitoring.mqtttoqueueconnector.queue.google

import flock.community.office.monitoring.mqtttoqueueconnector.queue.Subscriber
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate

class GCPPubSubSubscriber(private val pubSubTemplate: PubSubTemplate) : Subscriber {

    override fun subscribe(topic: String) {
        val message = pubSubTemplate.pull(topic, 1, true)
    }
}