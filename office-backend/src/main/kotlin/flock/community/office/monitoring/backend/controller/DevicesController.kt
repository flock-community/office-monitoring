package flock.community.office.monitoring.backend.controller

import flock.community.office.monitoring.backend.configuration.DeviceType
import flock.community.office.monitoring.backend.configuration.devicesMappingConfigurations
import kotlinx.coroutines.flow.flow
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class Devices {

    @MessageMapping("devices")
    fun getDevices() = flow {

        devicesMappingConfigurations.forEach { (id, config) ->
            val device = Device(id, config.description, config.deviceType)
            emit(device)
        }
    }
}

data class Device(
    val id: String,
    val name: String,
    val type: DeviceType
)
