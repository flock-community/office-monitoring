package flock.community.office.monitoring.backend.domain.gcp

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate

internal class CommandPublisherTest() {

    private val pubSubTemplate: PubSubTemplate = mock(PubSubTemplate::class.java)
    private val pubSubConfig: PubSubConfig = PubSubConfig("","")
    private val objectMapper = ObjectMapper()

    private val commandPublisher = CommandPublisher(pubSubTemplate, pubSubConfig, objectMapper)

    @Test
    fun `test on state command`() {
        commandPublisher.on("190f801d-2c55-487b-b44a-19ca61c432df")
    }
}