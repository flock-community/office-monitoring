package flock.community.office.monitoring.backend.controller

import flock.community.office.monitoring.backend.ContactSensorMessageDTO
import flock.community.office.monitoring.backend.DeviceState
import flock.community.office.monitoring.backend.DeviceStateHistoryService
import flock.community.office.monitoring.backend.UpdatesModel
import flock.community.office.monitoring.backend.configuration.DeviceType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import org.slf4j.LoggerFactory.getLogger
import org.springframework.messaging.Message
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.stereotype.Controller
import java.time.ZonedDateTime


data class Request(
    val since: ZonedDateTime? = null,
    val deviceType: DeviceType? = null,
    val deviceId: String? = null
)

@ExperimentalCoroutinesApi
@Controller
internal class StreamRoot(
    private val historyService: DeviceStateHistoryService,
    private val updatesModel: UpdatesModel
) {

    companion object {
        private val log = getLogger(this::class.java)
    }

    private val starterMessage =
        DeviceState(
            0, "test", "test_id",
            ContactSensorMessageDTO(ZonedDateTime.now(), -1, -1, false)
        )




    // TODO, handle request params
    // startTime
    // deviceType
    // deviceId
    @MessageMapping("start")
    internal fun start(message: Request, headerAccessor: SimpMessageHeaderAccessor): Flow<DeviceState> {
        val attrs = headerAccessor.sessionAttributes

        log.info("Received request for DeviceState: $message, attr: $attrs")
        val history = flow { historyService.getHistory().map { emit(it) } }
        return listOf(
            history,
            updatesModel.state
        )
            .merge()
            .onStart { emit(starterMessage) }
            .onEach { log.info("Sending to client: $it") }
    }
}
