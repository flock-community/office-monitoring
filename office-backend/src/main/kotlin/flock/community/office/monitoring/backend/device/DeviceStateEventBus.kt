package flock.community.office.monitoring.backend.device

import flock.community.office.monitoring.backend.device.domain.DeviceState
import flock.community.office.monitoring.backend.device.domain.StateBody
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import org.springframework.stereotype.Service

@Service
class DeviceStateEventBus {

    private val _events: MutableSharedFlow<DeviceState<StateBody>> = MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    suspend fun publish(deviceState: DeviceState<StateBody>): Boolean {
        return _events.tryEmit(deviceState)
    }

    fun subscribe(sensorId: String?): Flow<DeviceState<StateBody>> = _events.asSharedFlow().run {
        if (sensorId != null) {
            filter { it.sensorId == sensorId }
        } else {
            this
        }
    }
}
