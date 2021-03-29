package flock.community.office.monitoring.backend.domain.service

import flock.community.office.monitoring.backend.configuration.devicesMappingConfigurations
import flock.community.office.monitoring.backend.controller.*
import flock.community.office.monitoring.backend.controller.FlockMonitorCommandBody.GetDeviceStateCommand
import flock.community.office.monitoring.backend.controller.FlockMonitorCommandBody.GetDevicesCommand
import flock.community.office.monitoring.backend.controller.FlockMonitorMessageBody.*
import flock.community.office.monitoring.backend.controller.FlockMonitorMessageType.*
import flock.community.office.monitoring.backend.domain.repository.mapping.DeviceStateMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import org.springframework.stereotype.Service

interface Executor<T: FlockMonitorCommandBody> {
    fun getFlow(command: T): Flow<FlockMonitorMessage>

}

@Service
class DevicesCommandExecutor: Executor<GetDevicesCommand>{

    override fun getFlow(command: GetDevicesCommand): Flow<FlockMonitorMessage> = flow {

        val devices = devicesMappingConfigurations.map { (id, config) ->
            Device(id, config.description, config.deviceType)
        }

        emit(FlockMonitorMessage(DEVICE_LIST_MESSAGE, DeviceListMessage(devices)))
    }
}

@Service
class DevicesFeedCommandExecutor(val deviceStateHistoryService: DeviceStateHistoryService,
                                 val deviceStateEventBus: DeviceStateEventBus): Executor<GetDeviceStateCommand> {

    override fun getFlow(command: GetDeviceStateCommand ): Flow<FlockMonitorMessage> = flow {

        deviceStateHistoryService.getHistory(command.deviceId, command.from).collect {
            emit(FlockMonitorMessage(DEVICE_STATE, DeviceStateMessage(it)))
        }

        deviceStateEventBus.subscribe(command.deviceId).collect {
            emit(FlockMonitorMessage(DEVICE_STATE, DeviceStateMessage(it)))
        }
    }
}
