package flock.community.office.monitoring.backend.alerting.client

import flock.community.office.monitoring.backend.utils.client.HttpServerException
import flock.community.office.monitoring.backend.utils.client.guard
import flock.community.office.monitoring.backend.utils.client.verifyHttpStatus
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitExchange

@Component
class SignalAlertClient(
    @Qualifier("SignalAlertWebClient") private val webClient: WebClient
) {

    suspend fun sendMessage(phoneNumber: String, message: String): Boolean = guard({ ex ->
        HttpServerException(
            "Unexpected error sending message to Signal Message processor: ${ex.message}",
            ex
        )
    }) {
        val m = SignalApiMessageDto(phoneNumber, message);

        webClient.post()
            .uri { it.path("/send").build() }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(m)
            .awaitExchange {
                it.verifyHttpStatus()
            }

        return true;
    }
}

