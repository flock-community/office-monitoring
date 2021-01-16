package flock.community.office.monitoring.backend.domain.service

import flock.community.office.monitoring.backend.configuration.devicesConfigurations
import flock.community.office.monitoring.backend.domain.exception.DeviceException
import flock.community.office.monitoring.backend.domain.repository.entities.DeviceStateEntity
import flock.community.office.monitoring.backend.domain.repository.DeviceStateRepository
import flock.community.office.monitoring.queue.message.SensorEventQueueMessage
import flock.community.office.monitoring.utils.logging.Loggable.Companion.logger
import org.springframework.stereotype.Service
import java.util.*

@Service
class EventSaveService(
    val deviceStateRepository: DeviceStateRepository
) {

    fun saveSensorEventQueueMessage(sensorEventQueueMessage: SensorEventQueueMessage) {

        val deviceConfiguration = devicesConfigurations[sensorEventQueueMessage.topic] ?: throw DeviceException.UnknownDevice(sensorEventQueueMessage.topic, sensorEventQueueMessage.message)

        val state = DeviceStateEntity(
            UUID.randomUUID().toString(),
            deviceConfiguration.deviceType,
            sensorEventQueueMessage.topic,
            sensorEventQueueMessage.received,
            sensorEventQueueMessage.message
        )

        deviceStateRepository.save(state).also {
            logger.debug("Saved device state to database")
        }

        // TODO publish on internal event bus for live messages
    }
}
