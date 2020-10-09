package flock.community.office.monitoring.backend

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Repository

@Repository
class DevicesRepository(
        private val objectMapper: ObjectMapper
) {
    companion object {
        private val log = getLogger(this::class.java)
    }

    fun receiveMessage(payload: ByteArray) {
        try {
            val deviceMessage = objectMapper.readValue(payload, DeviceMessageWrapperDTO::class.java)
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
        } catch (e: Exception) {
            log.error("Received message failed", e)
        }
    }
}