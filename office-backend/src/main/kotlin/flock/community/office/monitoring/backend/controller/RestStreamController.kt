package flock.community.office.monitoring.backend.controller

import flock.community.office.monitoring.backend.ContactSensorMessageDTO
import flock.community.office.monitoring.backend.DeviceState
import flock.community.office.monitoring.backend.UpdatesModel
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
internal class RestStreamController(private val updatesModel: UpdatesModel) {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    private val starterMessage = DeviceState(
        0, "test", "test_id", ContactSensorMessageDTO(ZonedDateTime.now(), -1, -1, false)
    )

    @GetMapping
    internal fun start(): Flow<DeviceState> {
        log.info("Receiving")
        return updatesModel.state.onStart { emit(starterMessage) }.distinctUntilChanged()
    }

}