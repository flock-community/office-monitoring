package flock.community.office.monitoring.backend.controller

import flock.community.office.monitoring.backend.DeviceMessageWrapperDTO
import flock.community.office.monitoring.backend.UpdatesModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.ZonedDateTime

@CrossOrigin(origins = ["https://office.flock.community.com"], maxAge = 3600)
@RestController
@RequestMapping(path = ["/device-updates"])
@ExperimentalCoroutinesApi
internal class RestStreamController(private val updatesModel: UpdatesModel) {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    private val starterMessage = flow {
        emit(DeviceMessageWrapperDTO("topic", ZonedDateTime.now(), "very first message"))
    }

    @GetMapping
    internal fun start(): Flow<DeviceMessageWrapperDTO> {
        log.info("Receiving")
        return updatesModel.state.onStart {  starterMessage }
    }

}