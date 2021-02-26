package flock.community.office.monitoring.backend.domain.service

import flock.community.office.monitoring.backend.configuration.devicesMappingConfigurations
import flock.community.office.monitoring.backend.controller.Device
import flock.community.office.monitoring.backend.controller.FlockMonitorCommandType
import kotlinx.coroutines.flow.flow
import org.springframework.stereotype.Service

interface Executor {
    fun canHandleCommand(commandType: FlockMonitorCommandType)
}

@Service
class DevicesCommandExecutor {
    fun getDevices() = flow {

        devicesMappingConfigurations.forEach { (id, config) ->
            val device = Device(id, config.description, config.deviceType)
            emit(device)
        }
    }
}

@Service
class DevicesFeedCommandExecutor {
}