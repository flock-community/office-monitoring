package flock.community.office.monitoring.backend

import com.fasterxml.jackson.databind.ObjectMapper
import flock.community.office.monitoring.backend.configuration.iotDeviceIdToInternalId
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository

@Component
class Queue(
        val channel: Channel<DeviceMessageWrapperDTO> = Channel()
)


@Repository
class DevicesRepository(
        private val objectMapper: ObjectMapper,
        private val repository: DeviceStateRepository,
        private val queue: Queue

) {
    companion object {
        private val log = getLogger(this::class.java)
    }

    fun receiveMessage(payload: ByteArray) {

        val deviceMessage = objectMapper.readValue(payload, DeviceMessageWrapperDTO::class.java)
        val deviceId = iotDeviceIdToInternalId[deviceMessage.topic]
        if (deviceId == null) {
            log.warn("Message unreadable / relevant:", deviceMessage)
            return
        }

        parseDevice(deviceMessage)

        val t = DeviceState(id = deviceId, iotDeviceId = deviceMessage.topic, state = String(payload))
        repository.save(t)

        // If parsing and saving to db was successful, return and start new coroutine for side effects
        GlobalScope.async {
            queue.channel.send(deviceMessage)
        }
    }

    private fun parseDevice(deviceMessage: DeviceMessageWrapperDTO) {
        when (deviceMessage.topic) {
            "zigbee2mqtt/0x00158d0004852389" -> {
                val contactSensor = objectMapper.readValue(deviceMessage.message, ContactSensorMessageDTO::class.java)
                log.info(contactSensor.toString())
            }

            "zigbee2mqtt/0x00158d00041193bb" -> {
                val tempSensor = objectMapper.readValue(deviceMessage.message, TemperatureSensorMessageDTO::class.java)
                log.info(tempSensor.toString())
            }
            "zigbee2mqtt/0x7cb03eaa0a093c6a" -> {
                val switch = objectMapper.readValue(deviceMessage.message, SwitchMessageDTO::class.java)
                log.info(switch.toString())
            }
            else -> log.info(deviceMessage.toString())
        }
    }
}