package flock.community.office.monitoring.backend.controller

import flock.community.office.monitoring.backend.*
import flock.community.office.monitoring.backend.configuration.DeviceType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import org.slf4j.LoggerFactory.getLogger
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.stereotype.Controller
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
    private val updatesModel: UpdatesModel
) {

    companion object {
        private val log = getLogger(this::class.java)
    }

    private val starterMessage =
        DeviceState(
            0, "test", DeviceType.SWITCH, ZonedDateTime.now(),
            ContactSensorMessageDTO(ZonedDateTime.now(), -1, -1, false)
        )


    // TODO, handle request params
    // startTime
    // deviceType
    // deviceId
    @MessageMapping("start")
    internal fun start(message: Request, headerAccessor: SimpMessageHeaderAccessor): Flow<DeviceState> {
        val attrs = headerAccessor.sessionAttributes

        log.info("Received request for DeviceState: $message, attr: $attrs")
        val history = flow { historyService.getHistory().map { emit(it) } }
        return listOf(
            history,
            flow{emit(UpdatesModel.nullValue)},
            updatesModel.state
        )
            .merge()
            .onEach { log.info("Sending to client: $it") }
    }

    @MessageMapping("mock")
    internal fun mockedData(message: Request): Flow<DeviceState> {
        val clientId = UUID.randomUUID()

        log.info("Received mock-request for DeviceState: $message")
        return flow {
            while (true) {
                emit(getRandomDeviceState(message))
                delay(5000)
            }
        }
            .onEach { log.info("Sending to client '$clientId': $it") }
    }

    private fun getRandomDeviceState(message: Request) = DeviceState(
        id = message.deviceId ?: "mocked-id",
        type = message.deviceType ?: DeviceType.CONTACT_SENSOR,
        timeStamp = ZonedDateTime.now(),
        state = getRandomState(message)
    )

    private fun getRandomState(message: Request) =
        when (message.deviceType) {
            DeviceType.TEMPERATURE_SENSOR -> TemperatureSensorMessageDTO(
                last_seen = ZonedDateTime.now(),
                battery = Random.nextInt(0, 100),
                voltage = Random.nextInt(0, 100),
                humidity = Random.nextDouble(0.0, 100.0),
                pressure = Random.nextInt(0, 100),
                temperature = Random.nextDouble(0.0, 100.0)
            )
            DeviceType.SWITCH -> SwitchMessageDTO(
                last_seen = ZonedDateTime.now(),
                state = listOf("on", "off").random()
            )
            //DeviceType.CONTACT_SENSOR
            else -> ContactSensorMessageDTO(
                last_seen = ZonedDateTime.now(),
                battery = Random.nextInt(0, 100),
                voltage = Random.nextInt(0, 100),
                contact = Random.nextBoolean()
            )
        }
}
