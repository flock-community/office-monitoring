package flock.community.office.monitoring.backend.domain.exception

import flock.community.office.monitoring.backend.configuration.DeviceType

sealed class DeviceException(override val message: String, cause: Throwable? = null) : Throwable(message, cause) {

    data class UnknownDevice(val topic: String, val deviceMessage: String) : DeviceException("Data from unmapped device received, topic: $topic, message: $deviceMessage")
    data class UnmappedDeviceType(val type: DeviceType) : DeviceException("Cannot map state of device type: $type to a state body")
}
