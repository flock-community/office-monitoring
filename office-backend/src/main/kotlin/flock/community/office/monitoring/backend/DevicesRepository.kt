package flock.community.office.monitoring.backend

import com.fasterxml.jackson.databind.ObjectMapper
import flock.community.office.monitoring.backend.configuration.DeviceType
import flock.community.office.monitoring.backend.configuration.InternalDevice
import flock.community.office.monitoring.backend.configuration.iotDeviceIdToInternalId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.time.ZonedDateTime

@ExperimentalCoroutinesApi
@Component
class UpdatesModel {

    private val log = getLogger(javaClass)
    private var _state = MutableStateFlow(nullValue)
    val state: StateFlow<DeviceState> get() = _state

    fun update(deviceMessage: DeviceState) {
        log.info("Updating state with $deviceMessage")
        _state.value = deviceMessage
    }

    companion object {
        private val nullValue = DeviceState(null, "", "",
            ContactSensorMessageDTO(ZonedDateTime.now(), 0, 0, false)
        )
    }
}

@Repository
@ExperimentalCoroutinesApi
class DevicesRepository(
    private val objectMapper: ObjectMapper,
    private val deviceStateRepository: DeviceStateRepository,
    private val updatesModel: UpdatesModel

) {
    companion object {
        private val log = getLogger(this::class.java)
    }

    fun receiveMessage(payload: ByteArray) {
        try {
            val deviceMessage = objectMapper.readValue(payload, DeviceMessageWrapperDTO::class.java)
            val (deviceConfig, messageDTO) = parseDevice(deviceMessage)

            val deviceState = DeviceState(
                id = deviceConfig.id,
                iotDeviceId = deviceMessage.topic,
                state = objectMapper.writeValueAsString(messageDTO)
            )

            deviceStateRepository.save(deviceState)
            GlobalScope.async {
                updatesModel.update(deviceState)
            }
        } catch (e: Exception) {
            log.error("Something went wrong trying to process message $payload", e)
        }
    }

    private fun parseDevice(message: DeviceMessageWrapperDTO): Pair<InternalDevice, GenericDeviceMessageDTO> {
        val internalDevice = iotDeviceIdToInternalId[message.topic]
            ?: throw IllegalArgumentException("Message received on unconfigured topic")

        val deviceClass = when (internalDevice.type) {
            DeviceType.SWITCH -> SwitchMessageDTO::class.java
            DeviceType.CONTACT_SENSOR -> ContactSensorMessageDTO::class.java
            DeviceType.TEMPERATURE_SENSOR -> TemperatureSensorMessageDTO::class.java
        }

        log.debug("Received message of type $deviceClass: $message")
        return internalDevice to objectMapper.readValue(message.message, deviceClass)

    }
}
