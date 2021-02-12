package flock.community.office.monitoring.backend.controller

import flock.community.office.monitoring.backend.configuration.devicesMappingConfigurations
import flock.community.office.monitoring.backend.controller.FlockMonitorCommandBody.GetDeviceStateCommand
import flock.community.office.monitoring.backend.controller.FlockMonitorCommandType.GET_DEVICES_COMMAND
import flock.community.office.monitoring.backend.controller.FlockMonitorCommandType.GET_DEVICE_STATE_COMMAND
import flock.community.office.monitoring.backend.controller.FlockMonitorMessageBody.DeviceStateMessage
import flock.community.office.monitoring.backend.controller.FlockMonitorMessageType.DEVICE_STATE
import flock.community.office.monitoring.backend.domain.repository.mapping.DeviceStateMapper
import flock.community.office.monitoring.backend.domain.service.DeviceStateEventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class Devices(
    private val deviceStateEventBus: DeviceStateEventBus,
    private val deviceStateMapper: DeviceStateMapper
) {

    @MessageMapping("devices")
    fun getDevices() = flow {

        devicesMappingConfigurations.forEach { (id, config) ->
            val device = Device(id, config.description, config.deviceType)
            emit(device)
        }
    }


    // WIP
    // Think about concurrency, schedulers and those kind of things!
    // Should stream end?
    @MessageMapping("42")
    suspend fun theStream(commands: Flow<FlockMonitorCommand>): Flow<FlockMonitorMessage> {
        val activeStreams: MutableMap<String, Flow<FlockMonitorMessage>> = mutableMapOf()
        val publishStream: MutableSharedFlow<FlockMonitorMessage> = MutableSharedFlow()

        val commandProcess = commands.onEach { command ->
            val (id, newFlow) = when (command.type) {
                GET_DEVICES_COMMAND -> "" to null
                GET_DEVICE_STATE_COMMAND -> processDeviceStateCommand(command.body as GetDeviceStateCommand)
            }

            if (newFlow == null) {
                activeStreams.remove(id)
            } else {
                activeStreams[id] = newFlow
                newFlow
                    .takeWhile { activeStreams.containsKey(id) }
                    .onEach { publishStream.emit(it) }
            }
        }
        // Do something about this
        commandProcess.flowOn(Dispatchers.Default)

        return publishStream
    }

    private fun processDeviceStateCommand(command: GetDeviceStateCommand): Pair<String, Flow<FlockMonitorMessage>?> =
        "$GET_DEVICES_COMMAND-${command.deviceId}" to deviceStateEventBus.subscribe(command.deviceId)
            .map {
                FlockMonitorMessage(
                    type = DEVICE_STATE,
                    body = DeviceStateMessage(deviceStateMapper.map(it))
                )
            }

}
