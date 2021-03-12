package flock.community.office.monitoring.backend.domain.service

import flock.community.office.monitoring.backend.domain.model.DeviceState
import flock.community.office.monitoring.backend.domain.model.StateBody
import flock.community.office.monitoring.backend.domain.repository.entities.DeviceStateEntity
import kotlinx.coroutines.flow.*
import org.springframework.stereotype.Service

@Service
class DeviceStateEventBus() {

    private val _events: MutableSharedFlow<DeviceState<StateBody>> = MutableSharedFlow(replay = 1)

    suspend fun publish(deviceState: DeviceState<StateBody>) {
        _events.emit(deviceState)
    }

    fun subscribe(deviceId: String?): Flow<DeviceState<StateBody>> {
        return if (deviceId != null) {
            _events.asSharedFlow().filter { it.deviceId == deviceId }
        } else {
            _events.asSharedFlow()
        }
    }
}
