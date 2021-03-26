package flock.community.office.monitoring.backend.domain.service

import flock.community.office.monitoring.backend.controller.FlockMonitorCommandBody
import flock.community.office.monitoring.backend.controller.FlockMonitorCommandBody.GetDeviceStateCommand
import flock.community.office.monitoring.backend.controller.FlockMonitorCommandBody.GetDevicesCommand
import flock.community.office.monitoring.backend.controller.FlockMonitorMessage
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Service

@Service
class CommandDispatcher(val devicesCommandExecutor: DevicesCommandExecutor,
                        val devicesFeedCommandExecutor: DevicesFeedCommandExecutor) {

    fun dispatchCommand(command: FlockMonitorCommandBody): Flow<FlockMonitorMessage> {
        return when (command) {
            is GetDeviceStateCommand -> devicesFeedCommandExecutor.getFlow(command)
            is GetDevicesCommand -> devicesCommandExecutor.getFlow(command)
        }
    }

}