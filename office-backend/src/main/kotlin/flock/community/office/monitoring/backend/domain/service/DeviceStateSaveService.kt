package flock.community.office.monitoring.backend.domain.service

import flock.community.office.monitoring.backend.configuration.devicesMappingConfigurations
import flock.community.office.monitoring.backend.domain.exception.DeviceException
import flock.community.office.monitoring.backend.domain.repository.DeviceStateRepository
import flock.community.office.monitoring.backend.domain.repository.entities.DeviceStateEntity
import flock.community.office.monitoring.queue.message.DeviceStateEventQueueMessage
import flock.community.office.monitoring.utils.logging.loggerFor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import java.util.*

@Service
class DeviceStateSaveService(
    val deviceStateRepository: DeviceStateRepository,
    val deviceStateEventBus: DeviceStateEventBus
) {

    val logger = loggerFor<DeviceStateSaveService>()

    suspend fun saveSensorEventQueueMessage(deviceStateEventQueueMessage: DeviceStateEventQueueMessage) {

        val deviceConfiguration =
            devicesMappingConfigurations[deviceStateEventQueueMessage.topic] ?: throw DeviceException.UnknownDevice(deviceStateEventQueueMessage.topic, deviceStateEventQueueMessage.message)

        val deviceStateEntity = DeviceStateEntity(
            UUID.randomUUID().toString(),
            deviceConfiguration.deviceType,
            deviceStateEventQueueMessage.topic,
            deviceStateEventQueueMessage.received,
            deviceStateEventQueueMessage.message
        )

        deviceStateRepository.save(deviceStateEntity).also {
            logger.debug("Saved device state to database: $it")
        }

         deviceStateEventBus.publish(deviceStateEntity)
    }
}
