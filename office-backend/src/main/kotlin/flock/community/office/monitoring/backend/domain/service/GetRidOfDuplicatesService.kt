package flock.community.office.monitoring.backend.domain.service

import flock.community.office.monitoring.backend.configuration.devicesMappingConfigurations
import flock.community.office.monitoring.backend.domain.model.DeviceState
import flock.community.office.monitoring.backend.domain.model.StateBody
import flock.community.office.monitoring.backend.domain.repository.DeviceStateRepository
import flock.community.office.monitoring.backend.domain.repository.entities.DeviceStateEntity
import flock.community.office.monitoring.backend.domain.repository.mapping.DeviceStateMapper
import flock.community.office.monitoring.utils.logging.loggerFor
import org.springframework.stereotype.Service

@Service
class GetRidOfDuplicatesService(
    private val deviceStateRepository: DeviceStateRepository,
    private val deviceStateMapper: DeviceStateMapper

    ) {

    private val logger = loggerFor<DeviceStateHistoryService>()


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

            logger.info("Marked for deletion: ${markedForDeletion.size}")
            val deviceStateEntities: List<DeviceStateEntity> = markedForDeletion.map { d -> deviceStateMapper.map(d) }
            deviceStateRepository.deleteAll(deviceStateEntities)
//            logger.info("not deleting for now")
        }
    }

}