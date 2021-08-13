package flock.community.office.monitoring.backend.device.domain.exception

import flock.community.office.monitoring.backend.device.configuration.DeviceType

sealed class DeviceException(override val message: String, cause: Throwable? = null) : Throwable(message, cause) {

    data class UnknownDevice(val topic: String, val deviceMessage: String? = null) : DeviceException("Data from unmapped device received, topic: $topic, message: '${deviceMessage.orEmpty()}'")
    data class UnmappedDeviceType(val type: DeviceType) : DeviceException("Cannot map state of device type: $type to a state body")
}
