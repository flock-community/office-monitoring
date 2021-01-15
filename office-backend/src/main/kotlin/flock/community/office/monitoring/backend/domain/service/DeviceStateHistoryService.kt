package flock.community.office.monitoring.backend.domain.service

import flock.community.office.monitoring.backend.controller.DeviceMessageWrapperDTO
import flock.community.office.monitoring.backend.domain.model.DeviceState
import flock.community.office.monitoring.backend.domain.repository.DeviceStateRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.ZonedDateTime

@Component
class DeviceStateHistoryService(private val repository: DeviceStateRepository){


    private val log = LoggerFactory.getLogger(javaClass)

    fun getHistory(): List<DeviceMessageWrapperDTO> {
        val findAll: MutableIterable<DeviceState> = repository.findAll()
        return findAll.take(10).map{
            DeviceMessageWrapperDTO("history", ZonedDateTime.now(), it.state)
        }
    }

}
