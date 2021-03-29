package flock.community.office.monitoring.backend.domain.service

import flock.community.office.monitoring.backend.domain.model.DeviceState
import flock.community.office.monitoring.backend.domain.model.StateBody
import flock.community.office.monitoring.backend.domain.repository.DeviceStateRepository
import flock.community.office.monitoring.backend.domain.repository.mapping.DeviceStateMapper
import flock.community.office.monitoring.utils.logging.loggerFor
import kotlinx.coroutines.flow.*
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class DeviceStateHistoryService(
    private val repository: DeviceStateRepository,
    private val deviceStateMapper: DeviceStateMapper,
) {

    private val logger = loggerFor<DeviceStateHistoryService>()

    fun getHistory(deviceId: String, from: Instant): Flow<DeviceState<StateBody>> {
        logger.info("Start fetching DeviceState history")
        val findAll = repository.findTop100ByDeviceIdOrderByDateDesc(deviceId)
        logger.info("Done fetching DeviceState history")
        return findAll.asFlow()
            .catch { logger.info("Error fetching DeviceState history") }
            .map { deviceStateMapper.map(it) }
    }


}


