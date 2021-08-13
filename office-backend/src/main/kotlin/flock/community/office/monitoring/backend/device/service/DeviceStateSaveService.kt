package flock.community.office.monitoring.backend.device.service

import flock.community.office.monitoring.backend.device.DeviceStateEventBus
import flock.community.office.monitoring.backend.device.configuration.DeviceMappingConfiguration
import flock.community.office.monitoring.backend.device.configuration.devicesMappingConfigurations
import flock.community.office.monitoring.backend.device.domain.exception.DeviceException.UnknownDevice
import flock.community.office.monitoring.backend.device.repository.DeviceStateEntity
import flock.community.office.monitoring.backend.device.repository.DeviceStateMapper
import flock.community.office.monitoring.backend.device.repository.DeviceStateRepository
import flock.community.office.monitoring.queue.message.DeviceStateEventQueueMessage
import flock.community.office.monitoring.utils.logging.loggerFor
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class DeviceStateSaveService(
    val deviceStateRepository: DeviceStateRepository,
    val deviceStateEventBus: DeviceStateEventBus,
    val deviceStateMapper: DeviceStateMapper,
    @Value("\${device.device-state-updates.save-to-database}") private val saveToDatabase: Boolean,
) {

    val logger = loggerFor<DeviceStateSaveService>()

    suspend fun saveSensorEventQueueMessage(deviceStateEventQueueMessage: DeviceStateEventQueueMessage) {

        val deviceConfiguration =
            devicesMappingConfigurations[deviceStateEventQueueMessage.topic]
                ?: throw UnknownDevice(deviceStateEventQueueMessage.topic, deviceStateEventQueueMessage.message)

        deviceStateEventQueueMessage.toEntity(deviceConfiguration).also { entity ->
            entity.save()
            entity.publish()
        }
    }

    private fun DeviceStateEntity.save() {
        if (saveToDatabase) {
            deviceStateRepository.save(this).also {
                logger.debug("Saved device state to database: $this")
            }
        } else logger.debug("Not saving DeviceStateEntity, saving to database is not enabled")
    }

    private suspend fun DeviceStateEntity.publish() {
        deviceStateEventBus.publish(deviceStateMapper.map(this))
    }

    private fun DeviceStateEventQueueMessage.toEntity(device: DeviceMappingConfiguration) = DeviceStateEntity(
        id = UUID.randomUUID().toString(),
        type = device.deviceType,
        deviceId = topic,
        date = received,
        state = message
    )
}
