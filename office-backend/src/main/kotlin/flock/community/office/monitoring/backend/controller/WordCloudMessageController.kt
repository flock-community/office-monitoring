package flock.community.office.monitoring.backend.controller

import flock.community.office.monitoring.backend.DeviceMessageWrapperDTO
import flock.community.office.monitoring.backend.Queue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.reactor.asFlux
import kotlinx.coroutines.reactor.flux
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory.getLogger
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.BufferOverflowStrategy
import reactor.core.publisher.Flux


@ExperimentalCoroutinesApi
@Controller
internal class StreamRoot(private val queue: Queue) {

    companion object {
        private val log = getLogger(this::class.java)
    }

    @MessageMapping("start")
    internal fun getWords(): Flux<DeviceMessageWrapperDTO> {
        log.info("Receiving")
        return queue.channel.receiveAsFlow().asFlux()


    }
}
