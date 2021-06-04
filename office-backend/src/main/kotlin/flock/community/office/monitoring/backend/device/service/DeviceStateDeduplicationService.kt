package flock.community.office.monitoring.backend.device.service

import flock.community.office.monitoring.backend.device.configuration.devicesMappingConfigurations
import flock.community.office.monitoring.backend.device.domain.DeviceState
import flock.community.office.monitoring.backend.device.domain.StateBody
import flock.community.office.monitoring.backend.device.repository.DeviceStateRepository
import flock.community.office.monitoring.backend.device.repository.DeviceStateEntity
import flock.community.office.monitoring.backend.device.repository.DeviceStateMapper
import flock.community.office.monitoring.utils.logging.loggerFor
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class DeviceStateDeduplicationService(
    private val deviceStateRepository: DeviceStateRepository,
    private val deviceStateMapper: DeviceStateMapper
    ) {

    private val logger = loggerFor<DeviceStateHistoryService>()


    @Scheduled(fixedDelay = 24 * 60 * 60 * 1000) // once per day (in milliseconds)"
    fun deduplicate(){
        logger.warn("Running a dedupe ...")
        devicesMappingConfigurations.entries.map {
            val devicesStates: List<DeviceState<StateBody>> = deviceStateRepository.findTop100ByDeviceIdOrderByDateDesc(it.key)
                .map { d -> deviceStateMapper.map(d) }

            val markedForDeletion = mutableListOf<DeviceState<StateBody>>()
            devicesStates.zipWithNext{its, its2 ->
                when {
                    its == its2 -> {
                        logger.warn("Two identical items: ${its.state} and ${its2.state}")
                        markedForDeletion.add(its2);
                    }
                    its.equalsWithoutDateAndId(its2) -> {
                        logger.warn("Two items are the same (other than id/date), but send directly after one another: ${its.state} and ${its2.state}")
                        markedForDeletion.add(its2);
                    }
                    else  -> {
                        logger.debug("Not the same")
                    }
                }
            }

            logger.warn("Marked for deletion: ${markedForDeletion.size} entries")
            val deviceStateEntities: List<DeviceStateEntity> = markedForDeletion.map { d -> deviceStateMapper.map(d) }
            deviceStateRepository.deleteAll(deviceStateEntities)
        }
    }
}
