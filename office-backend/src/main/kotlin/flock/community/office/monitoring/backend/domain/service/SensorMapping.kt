package flock.community.office.monitoring.backend.domain.service

data class DeviceConfiguration(val type: DeviceType, val description: String)

val devicesConfigurations = mapOf(
    "zigbee2mqtt/0x00158d000578385c" to DeviceConfiguration(DeviceType.CONTACT, "TODO"),
    "zigbee2mqtt/0x00158d0004852389" to DeviceConfiguration(DeviceType.CONTACT, "TODO")
)

enum class DeviceType {
    CONTACT,
    TEMPERATURE,
    SWITCH,
    MOTION
}
