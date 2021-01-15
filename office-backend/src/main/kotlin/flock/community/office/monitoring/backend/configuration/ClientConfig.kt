package flock.community.office.monitoring.backend.configuration

import flock.community.office.monitoring.backend.DevicesRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate
import org.springframework.cloud.gcp.pubsub.integration.AckMode
import org.springframework.cloud.gcp.pubsub.integration.inbound.PubSubInboundChannelAdapter
import org.springframework.cloud.gcp.pubsub.support.BasicAcknowledgeablePubsubMessage
import org.springframework.cloud.gcp.pubsub.support.GcpPubSubHeaders
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.integration.channel.DirectChannel
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageHandler


@Configuration
class ClientConfig(
        private val devicesRepository: DevicesRepository,
        @Value("\${pubsub.subscription-name}")
        private val subscriptionName : String
) {

    companion object {
        private val log = getLogger(this::class.java)
        private const val INPUT_CHANNEL = "pubsubInputChannel"
    }

    @Bean
    fun messageChannelAdapter(
            @Qualifier(INPUT_CHANNEL) inputChannel: MessageChannel,
            pubSubTemplate: PubSubTemplate
    ): PubSubInboundChannelAdapter {
        val adapter = PubSubInboundChannelAdapter(pubSubTemplate, subscriptionName)
        adapter.outputChannel = inputChannel
        adapter.ackMode = AckMode.MANUAL
        return adapter
    }

    @Bean
    fun pubsubInputChannel(): MessageChannel? {
        return DirectChannel()
    }

    @Bean
    @ServiceActivator(inputChannel = INPUT_CHANNEL)
    fun messageReceiver(): MessageHandler = runBlocking {
        MessageHandler { message ->
            log.info("Received a message: $message")
            devicesRepository.receiveMessage(message.payload as ByteArray)

            val originalMessage = message.headers.get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage::class.java)
            originalMessage?.ack()

        }
    }
}
