package flock.community.office.monitoring.backend.controller

import flock.community.office.monitoring.backend.eventbus.DeviceStateEventBus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.ZonedDateTime

@RestController
@RequestMapping(path = ["/device-updates"])
@ExperimentalCoroutinesApi
internal class RestStreamController(private val deviceStateEventBus: DeviceStateEventBus) {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    private val starterMessage = flow {
        emit(DeviceMessageWrapperDTO("topic", ZonedDateTime.now(), "very first message"))
    }

    @GetMapping
    internal fun start(): Flow<DeviceMessageWrapperDTO> {
        log.info("Receiving")
        return deviceStateEventBus.state.onStart {  starterMessage }
    }

}
