package flock.community.office.monitoring.backend.domain.service

import flock.community.office.monitoring.backend.domain.model.DeviceState
import flock.community.office.monitoring.backend.domain.model.StateBody
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service

@Service
class DeviceStateEventBus {

    private val _events: MutableSharedFlow<DeviceState<StateBody>> = MutableSharedFlow(replay = 1)

    fun publish(deviceState: DeviceState<StateBody>) {
        GlobalScope.launch { _events.emit(deviceState) }
    }

    fun subscribe(sensorId: String?): Flow<DeviceState<StateBody>> {
        return if (sensorId != null) {
            _events.asSharedFlow().filter { it.deviceId == sensorId }
        } else {
            _events.asSharedFlow()
        }
    }
}
