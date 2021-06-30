package flock.community.office.monitoring.backend.device.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import flock.community.office.monitoring.backend.device.controller.dto.FlockMonitorCommand
import flock.community.office.monitoring.backend.device.controller.dto.FlockMonitorCommandBody.GetDeviceStateCommand
import flock.community.office.monitoring.backend.device.controller.dto.FlockMonitorCommandBody.GetDevicesCommand
import flock.community.office.monitoring.backend.device.controller.dto.FlockMonitorCommandType
import flock.community.office.monitoring.backend.device.controller.dto.FlockMonitorMessage
import flock.community.office.monitoring.utils.logging.loggerFor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import java.util.UUID


@ExperimentalCoroutinesApi
@Controller
internal class DeviceController(
    private val subscriptionHandler: SubscriptionHandler,
    private val objectMapper: ObjectMapper
) {

    private val logger = loggerFor<DeviceController>()

    @MessageMapping("devices")
    suspend fun main(commands: Flow<FlockMonitorCommand>): Flow<FlockMonitorMessage> {
        val requestId = UUID.randomUUID();
        val monitorCommandBodies = commands
            .onEach { logger.info("Processing command ${it.type} for $requestId (${it.body})") }
            .map {
            when(it.type) {
                FlockMonitorCommandType.GET_DEVICES_COMMAND -> objectMapper.convertValue<GetDevicesCommand>(it.body)
                FlockMonitorCommandType.GET_DEVICE_STATE_COMMAND -> objectMapper.convertValue<GetDeviceStateCommand>(it.body)
            }

        }

        return subscriptionHandler.subscribeForCommands(monitorCommandBodies, requestId)
            .onStart { logger.info("Received request from $requestId") }
            .onCompletion { logger.info("Finished serving requests to $requestId") }
            .catch {
                logger.error("Error occurred for $requestId.", it)
                throw it
            }

    }
}
