package flock.community.office.monitoring.backend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import flock.community.office.monitoring.backend.UpdatesModel
import flock.community.office.monitoring.backend.configuration.DeviceType
import flock.community.office.monitoring.backend.controller.FlockMonitorCommandBody.*
import flock.community.office.monitoring.backend.domain.model.*
import flock.community.office.monitoring.backend.domain.service.DeviceStateHistoryService
import flock.community.office.monitoring.utils.logging.loggerFor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import java.time.Instant
import java.time.ZonedDateTime
import java.util.*
import kotlin.random.Random


data class Request(
    val since: ZonedDateTime? = null,
    val deviceType: DeviceType? = null,
    val deviceId: String? = null
)

@ExperimentalCoroutinesApi
@Controller
internal class StreamRoot(
    private val historyService: DeviceStateHistoryService,
    private val updatesModel: UpdatesModel,
    private val subscriptionHandler: SubscriptionHandler,
    private val objectMapper: ObjectMapper
) {

    private val logger = loggerFor<StreamRoot>()

    @MessageMapping("devices")
    suspend fun main(commands: Flow<FlockMonitorCommand>): Flow<FlockMonitorMessage> {

        val bodies = commands.map {

            when(it.type) {
                FlockMonitorCommandType.GET_DEVICES_COMMAND -> objectMapper.convertValue<GetDevicesCommand>(it.body)
                FlockMonitorCommandType.GET_DEVICE_STATE_COMMAND -> objectMapper.convertValue<GetDeviceStateCommand>(it.body)
            }

        }

        return subscriptionHandler.theStream(bodies)
    }

    // TODO, handle request params
    // startTime
    // deviceType
    // deviceId
    @MessageMapping("start")
    internal fun start(message: Request): Flow<DeviceState<StateBody>> = flow {
        logger.info("Received request for DeviceState: $message")

        historyService.getHistory().collect {
            emit(it)
        }

        // TODO also emit live messages

    }.onEach { logger.info("Sending: $it") }


    @MessageMapping("stringsSplit")
    fun stringsSplit(requests: Flow<String>): Flow<String> {
        return requests
            .onEach { logger.info("Next: $it") }
            .flatMapConcat { input: String ->
                flow {
//                emit(input)
                    for (i in input) {
                        emit("$i")
                    }
                }

            }
            .onEach { logger.info("Next char: $it") }
    }


    @MessageMapping("mock")
    internal fun mockedData(message: Request): Flow<DeviceState<StateBody>> {
        val clientId = UUID.randomUUID()

        logger.info("Received mock-request for DeviceState: $message")
        return flow {
            while (true) {
                emit(getRandomDeviceState(message))
                delay(5000)
            }
        }.onEach { logger.info("Sending to client '$clientId': $it") }
    }

    private fun getRandomDeviceState(message: Request) = DeviceState(
        id = UUID.randomUUID().toString(),
        deviceId = message.deviceId ?: "mocked-id",
        type = message.deviceType ?: DeviceType.CONTACT_SENSOR,
        date = Instant.now(),
        state = getRandomState(message)
    )

    private fun getRandomState(message: Request): StateBody =
        when (message.deviceType) {
            DeviceType.TEMPERATURE_SENSOR -> TemperatureSensorStateBody(
                lastSeen = Instant.now(),
                battery = Random.nextInt(0, 100),
                voltage = Random.nextInt(0, 100),
                humidity = Random.nextDouble(0.0, 100.0),
                pressure = Random.nextInt(0, 100),
                temperature = Random.nextDouble(0.0, 40.0)
            )
            DeviceType.SWITCH -> SwitchStateBody(
                lastSeen = Instant.now(),
                state = listOf("on", "off").random()
            )
            else -> ContactSensorStateBody(
                lastSeen = Instant.now(),
                battery = Random.nextInt(0, 100),
                voltage = Random.nextInt(0, 100),
                contact = Random.nextBoolean()
            )
        }
}
