package flock.community.office.monitoring.backend.controller

import flock.community.office.monitoring.backend.controller.FlockMonitorCommandBody.GetDeviceStateCommand
import flock.community.office.monitoring.backend.controller.FlockMonitorCommandType.GET_DEVICES_COMMAND
import flock.community.office.monitoring.backend.controller.FlockMonitorCommandType.GET_DEVICE_STATE_COMMAND
import flock.community.office.monitoring.backend.controller.FlockMonitorMessageBody.DeviceStateMessage
import flock.community.office.monitoring.backend.controller.FlockMonitorMessageType.DEVICE_STATE
import flock.community.office.monitoring.backend.domain.service.CommandDispatcher
import flock.community.office.monitoring.utils.logging.loggerFor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Service
import java.util.*

@Service
class SubscriptionHandler(
        private val commandDispatcher: CommandDispatcher
) {

    private val logger = loggerFor<SubscriptionHandler>()

    // WIP
    // Think about concurrency, schedulers and those kind of things!
    // Should stream end?
    suspend fun theStream(commands: Flow<FlockMonitorCommandBody>): Flow<FlockMonitorMessage> = flow {

        val activeStreams: MutableMap<FlockMonitorCommandBody, Flow<FlockMonitorMessage>> = mutableMapOf()

        commands.collect { command ->

            if (activeStreams.containsKey(command)) {
                logger.info("The $command commmand is already active")
            } else {
                val commandFlow = commandDispatcher.dispatchCommand(command)
                activeStreams[command] = commandFlow
                emitAll(commandFlow)
            }
        }
    }

//    private fun processDeviceStateCommand(command: GetDeviceStateCommand): Pair<String, Flow<FlockMonitorMessage>?> =
//            "$GET_DEVICES_COMMAND-${command.deviceId}" to deviceStateEventBus.subscribe(command.deviceId)
//                    .map {
//                        FlockMonitorMessage(
//                                type = DEVICE_STATE,
//                                body = DeviceStateMessage(deviceStateMapper.map(it))
//                        )
//                    }

}
