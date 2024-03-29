package flock.community.office.monitoring.backend.device.controller

import flock.community.office.monitoring.backend.device.controller.dto.FlockMonitorCommandBody
import flock.community.office.monitoring.backend.device.controller.dto.FlockMonitorCommandBody.GetDeviceStateCommand
import flock.community.office.monitoring.backend.device.controller.dto.FlockMonitorCommandBody.GetDevicesCommand
import flock.community.office.monitoring.backend.device.controller.dto.FlockMonitorMessage
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Service

@Service
class CommandDispatcher(
    val devicesCommandExecutor: DevicesCommandExecutor,
    val deviceStatesCommandExecutor: DeviceStatesCommandExecutor
) {

    fun dispatchCommand(command: FlockMonitorCommandBody): Flow<FlockMonitorMessage> {
        return when (command) {
            is GetDeviceStateCommand -> deviceStatesCommandExecutor.getFlow(command)
            is GetDevicesCommand -> devicesCommandExecutor.getFlow(command)
        }
    }
}
