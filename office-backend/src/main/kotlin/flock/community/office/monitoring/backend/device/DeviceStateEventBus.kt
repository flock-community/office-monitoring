package flock.community.office.monitoring.backend.device

import flock.community.office.monitoring.backend.device.domain.DeviceState
import flock.community.office.monitoring.backend.device.domain.StateBody
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

@Service
class DeviceStateEventBus {

    private val _events: MutableSharedFlow<DeviceState<StateBody>> = MutableSharedFlow(replay = 1)

    fun publish(deviceState: DeviceState<StateBody>) {
        runBlocking { _events.tryEmit(deviceState) }
    }

    fun subscribe(sensorId: String?): Flow<DeviceState<StateBody>> {
        return if (sensorId != null) {
            _events.asSharedFlow().filter { it.sensorId == sensorId }
        } else {
            _events.asSharedFlow()
        }
    }
}
