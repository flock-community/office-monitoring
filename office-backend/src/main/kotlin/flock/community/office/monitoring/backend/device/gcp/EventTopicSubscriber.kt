package flock.community.office.monitoring.backend.device.gcp

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.pubsub.v1.SubscriptionAdminClient
import com.google.protobuf.Duration
import com.google.protobuf.FieldMask
import com.google.pubsub.v1.ExpirationPolicy
import com.google.pubsub.v1.Subscription
import com.google.pubsub.v1.UpdateSubscriptionRequest
import flock.community.office.monitoring.backend.device.service.DeviceStateSaveService
import flock.community.office.monitoring.queue.message.DeviceStateEventQueueMessage
import flock.community.office.monitoring.utils.logging.loggerFor
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.DisposableBean
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.cloud.gcp.pubsub.PubSubAdmin
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate
import org.springframework.cloud.gcp.pubsub.support.converter.JacksonPubSubMessageConverter
import org.springframework.cloud.gcp.pubsub.support.converter.PubSubMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import java.util.UUID

@ConstructorBinding
@ConfigurationProperties("pubsub")
data class PubSubConfig(
    val subscriptionName: String,
    val subscriptionPrefix: String,
    val topicName: String
) {
}

@Service
class EventTopicSubscriber(
    pubSubTemplate: PubSubTemplate,
    pubSubConfig: PubSubConfig,
    private val pubSubAdmin: PubSubAdmin,
    private val deviceStateSaveService: DeviceStateSaveService
) : DisposableBean{

    private val log = loggerFor<EventTopicSubscriber>()

    private val subscriptionName: String = "${pubSubConfig.subscriptionPrefix}--${UUID.randomUUID()}"

    init {
        val availableSubscriptions = pubSubAdmin.listSubscriptions()

        createInstanceUniqueSubscription(pubSubConfig)
        subscribeToSubscription(pubSubConfig, pubSubTemplate)

        log.info("new created subscriptions: ${pubSubAdmin.listSubscriptions().toSet().subtract(availableSubscriptions)}")
    }

    private fun createInstanceUniqueSubscription(pubSubConfig: PubSubConfig) {
        log.info("Creating subscription $subscriptionName for topic ${pubSubConfig.topicName}")

        // Create a new subscription for the configured topic
        val createSubscription = pubSubAdmin.createSubscription(subscriptionName, pubSubConfig.topicName)

        // Update the subscription to ensure timely removal of subscription in case application itself won't do it.
        val updatedSubscription = Subscription.newBuilder(createSubscription)
            .setMessageRetentionDuration(Duration.newBuilder().setSeconds(3600 * 10L).build())
            .setExpirationPolicy(
                ExpirationPolicy.newBuilder().setTtl(Duration.newBuilder().setSeconds(7200 * 24L).build()).build()
            )
            .build()

        // Send changes to GCP
        val admin = SubscriptionAdminClient.create()
        admin.updateSubscription(
            UpdateSubscriptionRequest.newBuilder().setSubscription(updatedSubscription)
                .setUpdateMask(
                    FieldMask.newBuilder().addAllPaths(listOf("message_retention_duration", "expiration_policy"))
                        .build()
                ).build()
        )

    }
    private fun subscribeToSubscription(
        pubSubConfig: PubSubConfig,
        pubSubTemplate: PubSubTemplate
    ) {
        log.info("Subscribing to $subscriptionName")
        pubSubTemplate.subscribeAndConvert(subscriptionName, { message ->
            runBlocking {
                try {
                    log.debug("Received SensorEventQueueMessage: ${message.pubsubMessage.messageId} - ${message.payload}")
                    deviceStateSaveService.saveSensorEventQueueMessage(message.payload)

                    message.ack().addCallback(
                        { log.debug("SensorEventQueueMessage acked : ${message.pubsubMessage.messageId} - ${message.payload}") },
                        { log.warn("Message ack failed ${message.pubsubMessage.messageId} - ${message.payload}") }
                    )
                } catch (e: Exception) {
                    log.warn("Something went wrong processing message ${message.pubsubMessage.messageId} ${message.payload}", e)
                    message.nack().addCallback(
                        { log.debug("SensorEventQueueMessage nacked ${message.pubsubMessage.messageId} - ${message.payload}") },
                        { log.warn("Message nack failed ${message.pubsubMessage.messageId} - ${message.payload}") }
                    )
                }
            }
        }, DeviceStateEventQueueMessage::class.java)
    }

    override fun destroy() {
        log.info("Shutting down '${this::class.simpleName}'")
        log.warn("trying to remove pubsub subscription")
        pubSubAdmin.deleteSubscription(subscriptionName)
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
