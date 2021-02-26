package flock.community.office.monitoring.backend.domain.service

import flock.community.office.monitoring.backend.configuration.devicesMappingConfigurations
import flock.community.office.monitoring.backend.controller.Device
import flock.community.office.monitoring.backend.controller.FlockMonitorCommandBody
import flock.community.office.monitoring.backend.controller.FlockMonitorCommandBody.GetDeviceStateCommand
import flock.community.office.monitoring.backend.controller.FlockMonitorCommandBody.GetDevicesCommand
import flock.community.office.monitoring.backend.controller.FlockMonitorMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.stereotype.Service

interface Executor<T: FlockMonitorCommandBody> {
    fun canHandleCommand(command: T): Boolean
    fun getFlow(command: T): Flow<FlockMonitorMessage>

}

@Service
class DevicesCommandExecutor: Executor<GetDevicesCommand>{

    override fun canHandleCommand(command: GetDevicesCommand): Boolean {
        TODO("Not yet implemented")
    }


    override fun getFlow(command: GetDevicesCommand): Flow<FlockMonitorMessage> = flow {

        devicesMappingConfigurations.forEach { (id, config) ->
            val device = Device(id, config.description, config.deviceType)
//            emit(device)
        }
    }
}

@Service
class DevicesFeedCommandExecutor(val deviceStateHistoryService: DeviceStateHistoryService,
                                 val deviceStateEventBus: DeviceStateEventBus): Executor<GetDeviceStateCommand> {

    override fun canHandleCommand(command: GetDeviceStateCommand): Boolean {
        TODO("Not yet implemented")
    }

    override fun getFlow(command: GetDeviceStateCommand ): Flow<FlockMonitorMessage> {
//        return deviceStateEventBus.subscribe(command.deviceId)
        TODO("Not yet implemented")
    }
}