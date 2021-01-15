package flock.community.office.monitoring.backend.eventbus

import flock.community.office.monitoring.backend.controller.DeviceMessageWrapperDTO
import flock.community.office.monitoring.utils.logging.Loggable.Companion.logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.springframework.stereotype.Component
import java.time.ZonedDateTime

@Component
class DeviceStateEventBus {

    private var _state = MutableStateFlow(DeviceMessageWrapperDTO("one,", ZonedDateTime.now(), "none"))
    val state: StateFlow<DeviceMessageWrapperDTO> get() = _state

    fun update(deviceMessage: DeviceMessageWrapperDTO) {
        logger.info("Updating state with $deviceMessage")
        _state.value = deviceMessage
    }
}
