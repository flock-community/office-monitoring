package flock.community.office.monitoring.backend.configuration

import flock.community.office.monitoring.backend.domain.model.ContactSensorStateBody
import flock.community.office.monitoring.backend.domain.model.StateBody
import flock.community.office.monitoring.backend.domain.model.SwitchStateBody
import flock.community.office.monitoring.backend.domain.model.TemperatureSensorStateBody
import kotlin.reflect.KClass

data class DeviceMappingConfiguration(val deviceType: DeviceType, val description: String)

val devicesMappingConfigurations = mapOf(
    "zigbee2mqtt/0x00158d000578385c" to DeviceMappingConfiguration(DeviceType.CONTACT_SENSOR, "Linker deur"),
    "zigbee2mqtt/0x00158d0004852389" to DeviceMappingConfiguration(DeviceType.CONTACT_SENSOR, "Rechter deur"),
    "zigbee2mqtt/0x00158d00047d493d" to DeviceMappingConfiguration(DeviceType.CONTACT_SENSOR, "Deur dakterras")
)

enum class DeviceType(val stateBody: KClass<out StateBody>) {
    CONTACT_SENSOR(ContactSensorStateBody::class),
    TEMPERATURE_SENSOR(TemperatureSensorStateBody::class),
    SWITCH(SwitchStateBody::class)
}
