package flock.community.office.monitoring.backend.domain.repository.mapping

import com.fasterxml.jackson.databind.ObjectMapper
import flock.community.office.monitoring.backend.configuration.DeviceType
import flock.community.office.monitoring.backend.domain.exception.DeviceException
import flock.community.office.monitoring.backend.domain.model.DeviceState
import flock.community.office.monitoring.backend.domain.model.StateBody
import flock.community.office.monitoring.backend.domain.repository.entities.DeviceStateEntity
import org.springframework.stereotype.Service

@Service
class DeviceStateMapper(
    private val objectMapper: ObjectMapper
) {

    fun map(entity: DeviceStateEntity): DeviceState<StateBody> {

        val deviceStateBody = DeviceType.values().find { it == entity.type }
            .let { deviceType ->
                deviceType ?: throw DeviceException.UnmappedDeviceType(entity.type)
                objectMapper.readValue(entity.state, deviceType.stateBody.java)
            }

        return DeviceState(
            id = entity.id,
            type = entity.type,
            deviceId = entity.deviceId,
            date = entity.date,
            state = deviceStateBody
        )
    }
}

