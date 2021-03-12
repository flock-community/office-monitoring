package flock.community.office.monitoring.backend.domain.service

import flock.community.office.monitoring.backend.domain.model.DeviceState
import flock.community.office.monitoring.backend.domain.model.StateBody
import flock.community.office.monitoring.backend.domain.repository.DeviceStateRepository
import flock.community.office.monitoring.backend.domain.repository.mapping.DeviceStateMapper
import flock.community.office.monitoring.utils.logging.loggerFor
import kotlinx.coroutines.flow.*
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class DeviceStateHistoryService(
    private val repository: DeviceStateRepository,
    private val deviceStateMapper: DeviceStateMapper,
) {

    private val logger = loggerFor<DeviceStateHistoryService>()

    fun getHistory(): Flow<DeviceState<StateBody>> {
        val findAll = repository.findAll(PageRequest.of(1,10))
        return findAll.asFlow()
            .onStart { logger.info("Start fetching DeviceState history") }
            .onCompletion { logger.info("Finished fetching DeviceSate history") }
            .catch { logger.info("Error fetching DeviceState history") }
            .take(10)
            .map { deviceStateMapper.map(it) }
    }


}


