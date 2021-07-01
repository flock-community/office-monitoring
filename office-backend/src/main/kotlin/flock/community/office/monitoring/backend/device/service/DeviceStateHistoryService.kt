package flock.community.office.monitoring.backend.device.service

import flock.community.office.monitoring.backend.device.domain.DeviceState
import flock.community.office.monitoring.backend.device.domain.StateBody
import flock.community.office.monitoring.backend.device.repository.DeviceStateRepository
import flock.community.office.monitoring.backend.device.repository.DeviceStateMapper
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

    fun getHistory(sensorId: String, from: Instant): Flow<DeviceState<StateBody>> {
        logger.debug("Start fetching DeviceState history for $sensorId, since $from")
        val findAll = repository.findAllByDeviceIdAndDateGreaterThanOrderByDateAsc(sensorId, from)
        logger.debug("Done fetching DeviceState history for $sensorId, since $from")
        return findAll.asFlow()
            .catch { logger.error("Error fetching DeviceState history", it) }
            .map { deviceStateMapper.map(it) }
    }

    fun getLatest(sensorId: String): DeviceState<StateBody>?{
        return repository.findTop1ByDeviceIdOrderByDateDesc(sensorId)
            .firstOrNull()?.let { deviceStateMapper.map(it) }

    }


}


