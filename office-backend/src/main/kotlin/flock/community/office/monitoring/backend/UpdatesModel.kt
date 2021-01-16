package flock.community.office.monitoring.backend

import flock.community.office.monitoring.backend.configuration.DeviceType
import flock.community.office.monitoring.backend.domain.repository.entities.DeviceStateEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

@ExperimentalCoroutinesApi
@Component
class UpdatesModel {

    private val log = getLogger(javaClass)
    private var _state = MutableStateFlow(nullValue)
    val state: StateFlow<DeviceStateEntity> get() = _state

    fun update(deviceMessage: DeviceStateEntity) {
        log.info("Updating state with $deviceMessage")
        _state.value = deviceMessage
    }

    companion object {
        val nullValue = DeviceStateEntity(
            id = "starter-message",
            deviceId = UUID.randomUUID().toString(),
            type = DeviceType.CONTACT_SENSOR,
            date = Instant.now(),
            state = ""
        )
    }
}
