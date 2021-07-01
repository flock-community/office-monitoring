package flock.community.office.monitoring.backend.device.controller.dto

import flock.community.office.monitoring.backend.device.domain.DeviceState
import flock.community.office.monitoring.backend.device.domain.StateBody

data class FlockMonitorMessage(
    val type: FlockMonitorMessageType,
    val body: FlockMonitorMessageBody
)

sealed class FlockMonitorMessageBody {
    data class DeviceListMessage(val devices: List<Device>) : FlockMonitorMessageBody()
    data class DeviceStateMessage(val state: DeviceState<StateBody>) : FlockMonitorMessageBody()
}

enum class FlockMonitorMessageType {
    DEVICE_LIST_MESSAGE,
    DEVICE_STATE
}
