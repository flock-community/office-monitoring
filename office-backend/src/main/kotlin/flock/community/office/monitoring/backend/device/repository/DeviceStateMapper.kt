package flock.community.office.monitoring.backend.device.repository

import com.fasterxml.jackson.databind.ObjectMapper
import flock.community.office.monitoring.backend.device.configuration.DeviceType
import flock.community.office.monitoring.backend.device.configuration.devicesMappingConfigurations
import flock.community.office.monitoring.backend.device.domain.exception.DeviceException
import flock.community.office.monitoring.backend.device.domain.DeviceState
import flock.community.office.monitoring.backend.device.domain.StateBody
import flock.community.office.monitoring.backend.device.domain.exception.DeviceException.*
import org.springframework.stereotype.Service

@Service
class DeviceStateMapper(
    private val objectMapper: ObjectMapper
) {

    fun map(entity: DeviceStateEntity): DeviceState<StateBody> {

        val deviceId = devicesMappingConfigurations[entity.deviceId]
            ?.deviceId
            ?: throw UnknownDevice(entity.deviceId)

        val deviceStateBody: StateBody = DeviceType.values().find { it == entity.type }
            .let { deviceType ->
                deviceType ?: throw UnmappedDeviceType(entity.type)
                objectMapper.readValue(entity.state, deviceType.stateBody.java)
            }

        return DeviceState(
            id = entity.id,
            type = entity.type,
            deviceId = deviceId,
            sensorId = entity.deviceId,
            date = entity.date,
            state = deviceStateBody
        )
    }

    fun map(deviceState: DeviceState<StateBody>) : DeviceStateEntity {
        return DeviceStateEntity(
            id = deviceState.id,
            type = deviceState.type,
            deviceId = deviceState.sensorId,
            date = deviceState.date,
            state = objectMapper.writeValueAsString(deviceState.state)
        )
    }

}
