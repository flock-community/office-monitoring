package flock.community.office.monitoring.backend.repository.mapping

import com.fasterxml.jackson.databind.ObjectMapper
import flock.community.office.monitoring.backend.configuration.DeviceType
import flock.community.office.monitoring.backend.domain.exception.DeviceException
import flock.community.office.monitoring.backend.domain.model.DeviceState
import flock.community.office.monitoring.backend.repository.model.DeviceStateEntity
import org.springframework.stereotype.Service

@Service
class DeviceStateMapper(
    private val objectMapper: ObjectMapper
) {

    fun map(entity: DeviceStateEntity): DeviceState<*> {

        val deviceStateBody = DeviceType.values().find { it == entity.type }
            .let { deviceType ->
                deviceType ?: throw DeviceException.UnmappedDeviceType(entity.type)
                objectMapper.readValue(entity.state, deviceType.stateBody.java)
            }

        return DeviceState(entity.id, entity.type, entity.deviceId, entity.date, deviceStateBody)
    }
}


