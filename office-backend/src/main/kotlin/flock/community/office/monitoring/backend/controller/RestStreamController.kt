package flock.community.office.monitoring.backend.controller

import flock.community.office.monitoring.backend.ContactSensorMessageDTO
import flock.community.office.monitoring.backend.DeviceState
import flock.community.office.monitoring.backend.UpdatesModel
import flock.community.office.monitoring.backend.configuration.DeviceType
import flock.community.office.monitoring.backend.eventbus.DeviceStateEventBus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.ZonedDateTime

@RestController
@RequestMapping(path = ["/device-updates"])
@ExperimentalCoroutinesApi
internal class RestStreamController(private val updatesModel: DeviceStateEventBus) {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    private val starterMessage = DeviceState(
        eventId = 0,
        id = "starter-message",
        type = DeviceType.SWITCH,
        timeStamp = ZonedDateTime.now(),
        state = ContactSensorMessageDTO(ZonedDateTime.now(), -1, -1, false)
    )

    @GetMapping
    internal fun start(): Flow<DeviceState> {
        log.info("Receiving")
        return updatesModel.state.onStart { emit(UpdatesModel.nullValue) }.distinctUntilChanged()
    }

}
