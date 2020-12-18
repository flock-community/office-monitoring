package flock.community.office.monitoring.backend.controller

import flock.community.office.monitoring.backend.DeviceMessageWrapperDTO
import flock.community.office.monitoring.backend.UpdatesModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import org.slf4j.LoggerFactory.getLogger
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import java.time.ZonedDateTime


@ExperimentalCoroutinesApi
@Controller
internal class StreamRoot(private val updatesModel: UpdatesModel) {

    companion object {
        private val log = getLogger(this::class.java)
    }

    private val starterMessage = flow {
        emit(DeviceMessageWrapperDTO("topic", ZonedDateTime.now(), "very first message"))
    }

    @MessageMapping("start")
    internal fun start(): Flow<DeviceMessageWrapperDTO> {
        log.info("Receiving")
        return updatesModel.state
            .onStart { starterMessage }
            .onEach { log.info("Sending to client: $it") }

//        Flux.just(DeviceMessageWrapperDTO("topic", ZonedDateTime.now(), "very first message"))
//                .concatWith {
//                    updatesModel.state.asFlux()
//                }

    }
}
