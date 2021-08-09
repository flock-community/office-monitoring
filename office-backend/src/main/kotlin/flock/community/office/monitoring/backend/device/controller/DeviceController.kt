package flock.community.office.monitoring.backend.device.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import flock.community.office.monitoring.backend.device.controller.dto.FlockMonitorCommand
import flock.community.office.monitoring.backend.device.controller.dto.FlockMonitorCommandBody
import flock.community.office.monitoring.backend.device.controller.dto.FlockMonitorCommandBody.GetDeviceStateCommand
import flock.community.office.monitoring.backend.device.controller.dto.FlockMonitorCommandBody.GetDevicesCommand
import flock.community.office.monitoring.backend.device.controller.dto.FlockMonitorCommandType.GET_DEVICES_COMMAND
import flock.community.office.monitoring.backend.device.controller.dto.FlockMonitorCommandType.GET_DEVICE_STATE_COMMAND
import flock.community.office.monitoring.backend.device.controller.dto.FlockMonitorMessage
import flock.community.office.monitoring.utils.logging.loggerFor
import kotlinx.coroutines.flow.*
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import java.util.*

@Controller
internal class DeviceController(
    private val subscriptionHandler: SubscriptionHandler,
    private val objectMapper: ObjectMapper
) {

    private val logger = loggerFor<DeviceController>()

    @MessageMapping("devices")
    suspend fun monitorMessages(commands: Flow<FlockMonitorCommand>): Flow<FlockMonitorMessage> {
        val requestId = UUID.randomUUID()
        return commands
            .onEach { logger.info("Processing command ${it.type} for $requestId (${it.body})") }
            .convertCommandBodies()
            .createSubscriptions(requestId)
    }

    private fun Flow<FlockMonitorCommand>.convertCommandBodies() = map {
        when (it.type) {
            GET_DEVICES_COMMAND -> objectMapper.convertValue<GetDevicesCommand>(it.body)
            GET_DEVICE_STATE_COMMAND -> objectMapper.convertValue<GetDeviceStateCommand>(it.body)
        }
    }

    private suspend fun Flow<FlockMonitorCommandBody>.createSubscriptions(requestId: UUID): Flow<FlockMonitorMessage> {
        return subscriptionHandler.subscribeForCommands(this, requestId)
            .onStart { logger.info("Received request from $requestId") }
            .onCompletion { logger.info("Finished serving requests to $requestId") }
            .catch {
                logger.error("Error occurred for $requestId.", it)
                throw it
            }
    }
}
