package flock.community.office.monitoring.backend.device.controller

import io.rsocket.transport.netty.client.WebsocketClientTransport
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.connectAndAwait
import org.springframework.messaging.rsocket.retrieveFlow
import org.springframework.util.MimeTypeUtils


internal abstract class AbstractTest(
    @Autowired
    private val builder: RSocketRequester.Builder
) {
    @Value("\${spring.rsocket.server.port}")
    private val serverPort = 0


    suspend fun createRSocketRequester(): RSocketRequester {
        return builder.dataMimeType(MimeTypeUtils.TEXT_PLAIN)
            .connectAndAwait(WebsocketClientTransport.create(serverPort))
    }
}

@Disabled
@SpringBootTest
internal class StringsSplitTest(
    @Autowired
    private val builder: RSocketRequester.Builder
) : AbstractTest(builder) {
    @Test
    @DisplayName("Test strings split")
    fun testStringsSplit() = runBlocking {
        val requester: RSocketRequester = createRSocketRequester()

        val response = requester.route("stringsSplit")
            .data(flow { emit("hello"); emit("world") })
            .retrieveFlow<String>()

        val emissions = response
            .onEach { println("Received next char: $it") }
            .toList()

        assertEquals(emissions, listOf("h", "e", "l", "l", "o", "w", "o", "r", "l", "d"))
    }
}
