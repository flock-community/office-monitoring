package flock.community.office.monitoring.backend.domain.service

import flock.community.office.monitoring.backend.domain.repository.entities.DeviceStateEntity
import kotlinx.coroutines.flow.*

class DeviceStateEventBus() {

    private val _events: MutableSharedFlow<DeviceStateEntity> = MutableSharedFlow(replay = 1)

    val events: SharedFlow<DeviceStateEntity> = _events.asSharedFlow()

    suspend fun publish(deviceState: DeviceStateEntity) {
        _events.emit(deviceState)
    }

    fun subscribe(deviceId: String): Flow<DeviceStateEntity> {
        return events.filter { it.deviceId == deviceId }
    }
}