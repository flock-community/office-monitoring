package flock.community.office.monitoring.backend.device.controller.dto

import com.fasterxml.jackson.databind.node.ObjectNode
import java.time.Instant

data class FlockMonitorCommand(
    val type: FlockMonitorCommandType,
    val body: ObjectNode
)

sealed class FlockMonitorCommandBody {
    abstract val key: String

    data class GetDevicesCommand(val id: String = "-1") : FlockMonitorCommandBody() {
        override val key: String
            get() = id
    }

    data class GetDeviceStateCommand(val deviceId: String, val from: Instant) : FlockMonitorCommandBody() {
        override val key: String
            get() = deviceId
    }
}

enum class FlockMonitorCommandType {
    // Commands
    GET_DEVICES_COMMAND,
    GET_DEVICE_STATE_COMMAND
}
