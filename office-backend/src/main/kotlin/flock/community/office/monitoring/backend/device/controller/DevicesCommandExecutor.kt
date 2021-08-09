package flock.community.office.monitoring.backend.device.controller

import flock.community.office.monitoring.backend.device.DeviceStateEventBus
import flock.community.office.monitoring.backend.device.configuration.devicesMappingConfigurations
import flock.community.office.monitoring.backend.device.controller.dto.Device
import flock.community.office.monitoring.backend.device.controller.dto.FlockMonitorCommandBody
import flock.community.office.monitoring.backend.device.controller.dto.FlockMonitorCommandBody.GetDeviceStateCommand
import flock.community.office.monitoring.backend.device.controller.dto.FlockMonitorCommandBody.GetDevicesCommand
import flock.community.office.monitoring.backend.device.controller.dto.FlockMonitorMessage
import flock.community.office.monitoring.backend.device.controller.dto.FlockMonitorMessageBody.DeviceListMessage
import flock.community.office.monitoring.backend.device.controller.dto.FlockMonitorMessageBody.DeviceStateMessage
import flock.community.office.monitoring.backend.device.controller.dto.FlockMonitorMessageType.DEVICE_LIST_MESSAGE
import flock.community.office.monitoring.backend.device.controller.dto.FlockMonitorMessageType.DEVICE_STATE
import flock.community.office.monitoring.backend.device.service.DeviceStateHistoryService
import flock.community.office.monitoring.utils.logging.loggerFor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import org.springframework.stereotype.Service

interface Executor<T : FlockMonitorCommandBody> {
    fun getFlow(command: T): Flow<FlockMonitorMessage>

}

@Service
class DevicesCommandExecutor : Executor<GetDevicesCommand> {

    override fun getFlow(command: GetDevicesCommand): Flow<FlockMonitorMessage> = flow {

        val devices = devicesMappingConfigurations.map { (sensorId, config) ->
            Device(config.deviceId, config.description, config.deviceType, sensorId)
        }

        emit(FlockMonitorMessage(DEVICE_LIST_MESSAGE, DeviceListMessage(devices)))
    }
}

@Service
class DeviceStatesCommandExecutor(
    val deviceStateHistoryService: DeviceStateHistoryService,
    val deviceStateEventBus: DeviceStateEventBus
) : Executor<GetDeviceStateCommand> {

    private val logger = loggerFor<DeviceStatesCommandExecutor>()

    override fun getFlow(command: GetDeviceStateCommand): Flow<FlockMonitorMessage> = flow {

        val sensorId = devicesMappingConfigurations.entries
            .firstOrNull { it.value.deviceId == command.deviceId }
            ?.key

        if (sensorId != null) {
            deviceStateHistoryService.getHistory(sensorId, command.from).collect {
                emit(FlockMonitorMessage(DEVICE_STATE, DeviceStateMessage(it)))
            }

            deviceStateEventBus.subscribe(command.deviceId).collect {
                emit(FlockMonitorMessage(DEVICE_STATE, DeviceStateMessage(it)))
            }
        } else {
            logger.warn("Device for ${command.deviceId} not found")
        }
    }
}
