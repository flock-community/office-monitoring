package flock.community.office.monitoring.backend.utils.client

import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.awaitBody

inline fun <T> guard(noinline errorBlock: (ex: Throwable) -> Throwable, block: () -> T): T {
    return try {
        block()
    } catch (t: Throwable) {
        throw errorBlock(t)
    }
}

inline fun <T> httpServerExceptionGuard(block: () -> T): T =
    guard({ e -> HttpServerException("Http guard caught unexpected expection: ${e.message}", e) }, block)

suspend fun ClientResponse.verifyHttpStatus() {
    when {
        statusCode() == HttpStatus.NOT_FOUND -> throw ResourceNotFoundException("Could not find resource")
        statusCode().is4xxClientError -> throw HttpClientException("Client exception occurred. StatusCode: ${statusCode()}. Body: ${awaitBody<String>()}")
        statusCode().is5xxServerError -> throw HttpServerException("Server exception occured. StatusCode: ${statusCode()}. Body: ${awaitBody<String>()}")
        else -> Unit
    }
}
