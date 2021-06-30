package flock.community.office.monitoring.backend.device.service

import flock.community.office.monitoring.backend.device.DeviceStateEventBus
import flock.community.office.monitoring.backend.device.configuration.devicesMappingConfigurations
import flock.community.office.monitoring.backend.device.domain.exception.DeviceException
import flock.community.office.monitoring.backend.device.repository.DeviceStateRepository
import flock.community.office.monitoring.backend.device.repository.DeviceStateEntity
import flock.community.office.monitoring.backend.device.repository.DeviceStateMapper
import flock.community.office.monitoring.queue.message.DeviceStateEventQueueMessage
import flock.community.office.monitoring.utils.logging.loggerFor
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class DeviceStateSaveService(
    val deviceStateRepository: DeviceStateRepository,
    val deviceStateEventBus: DeviceStateEventBus,
    val deviceStateMapper: DeviceStateMapper,
    @Value("\${device.device-state-updates.save-to-database}") private val saveToDatabase: Boolean,
) {

    val logger = loggerFor<DeviceStateSaveService>()

    fun saveSensorEventQueueMessage(deviceStateEventQueueMessage: DeviceStateEventQueueMessage) {

        val deviceConfiguration =
            devicesMappingConfigurations[deviceStateEventQueueMessage.topic] ?: throw DeviceException.UnknownDevice(deviceStateEventQueueMessage.topic, deviceStateEventQueueMessage.message)

        val deviceStateEntity = DeviceStateEntity(
            UUID.randomUUID().toString(),
            deviceConfiguration.deviceType,
            deviceStateEventQueueMessage.topic,
            deviceStateEventQueueMessage.received,
            deviceStateEventQueueMessage.message
        )


        if (saveToDatabase) {
            deviceStateRepository.save(deviceStateEntity).also {
                logger.debug("Saved device state to database: $it")
            }
        }

        deviceStateEventBus.publish(deviceStateMapper.map(deviceStateEntity))
    }
}
