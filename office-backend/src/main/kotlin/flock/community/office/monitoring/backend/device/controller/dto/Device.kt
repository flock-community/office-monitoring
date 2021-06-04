package flock.community.office.monitoring.backend.device.controller.dto

import flock.community.office.monitoring.backend.device.configuration.DeviceType

data class Device(
    val id: String,
    val name: String,
    val type: DeviceType,
    val sensorId: String
)
