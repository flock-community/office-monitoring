package flock.community.office.monitoring.backend.controller

import flock.community.office.monitoring.backend.DeviceMessageWrapperDTO
import flock.community.office.monitoring.backend.Queue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.reactor.asFlux
import org.slf4j.LoggerFactory.getLogger
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import java.time.ZonedDateTime


@ExperimentalCoroutinesApi
@Controller
internal class StreamRoot(private val queue: Queue) {

    companion object {
        private val log = getLogger(this::class.java)
    }

    @MessageMapping("start")
    internal fun getWords(): Flux<DeviceMessageWrapperDTO> {
        log.info("Receiving")
        return Flux.just(DeviceMessageWrapperDTO("topic", ZonedDateTime.now(), "very first message"))
                .concatWith {
                    queue.channel.receiveAsFlow()
                            .asFlux()
                }


    }
}
