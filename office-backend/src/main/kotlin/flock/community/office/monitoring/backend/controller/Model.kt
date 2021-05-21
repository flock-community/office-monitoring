package flock.community.office.monitoring.backend.controller

import com.fasterxml.jackson.databind.node.ObjectNode
import flock.community.office.monitoring.backend.configuration.DeviceType
import flock.community.office.monitoring.backend.domain.model.DeviceState
import flock.community.office.monitoring.backend.domain.model.StateBody
import java.time.Instant

data class FlockMonitorCommand (
    val type: FlockMonitorCommandType,
    val body: ObjectNode
)

data class FlockMonitorMessage (
    val type: FlockMonitorMessageType,
    val body: FlockMonitorMessageBody
)

sealed class FlockMonitorCommandBody  {
    data class GetDevicesCommand(val id: Int = -1) : FlockMonitorCommandBody()
    data class GetDeviceStateCommand(val deviceId: String, val from: Instant) : FlockMonitorCommandBody()
}

sealed class FlockMonitorMessageBody {
    data class DeviceListMessage(val devices: List<Device>) : FlockMonitorMessageBody()
    data class DeviceStateMessage(val state: DeviceState<StateBody>) : FlockMonitorMessageBody()
}


enum class FlockMonitorCommandType {
    // Commands
    GET_DEVICES_COMMAND,
    GET_DEVICE_STATE_COMMAND
}

enum class FlockMonitorMessageType {
    // Messages
    DEVICE_LIST_MESSAGE,
    DEVICE_STATE
}

data class Device(val id: String, val name: String, val type: DeviceType, val sensorId: String)
