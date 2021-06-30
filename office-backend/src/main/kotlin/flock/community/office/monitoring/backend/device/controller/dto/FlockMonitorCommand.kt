package flock.community.office.monitoring.backend.device.controller.dto

import com.fasterxml.jackson.databind.node.ObjectNode
import java.time.Instant

data class FlockMonitorCommand (
    val type: FlockMonitorCommandType,
    val body: ObjectNode
)

sealed class FlockMonitorCommandBody  {
    data class GetDevicesCommand(val id: Int = -1) : FlockMonitorCommandBody()
    data class GetDeviceStateCommand(val deviceId: String, val from: Instant) : FlockMonitorCommandBody()
}

enum class FlockMonitorCommandType {
    // Commands
    GET_DEVICES_COMMAND,
    GET_DEVICE_STATE_COMMAND
}
