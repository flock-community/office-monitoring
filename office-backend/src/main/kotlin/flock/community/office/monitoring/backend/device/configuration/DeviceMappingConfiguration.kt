package flock.community.office.monitoring.backend.device.configuration

import flock.community.office.monitoring.backend.device.domain.ContactSensorStateBody
import flock.community.office.monitoring.backend.device.domain.StateBody
import flock.community.office.monitoring.backend.device.domain.SwitchStateBody
import flock.community.office.monitoring.backend.device.domain.TemperatureSensorStateBody
import kotlin.reflect.KClass

data class DeviceMappingConfiguration(val deviceId: String, val deviceType: DeviceType, val description: String)

val devicesMappingConfigurations = mapOf(
    "zigbee2mqtt/0x00158d000578385c" to DeviceMappingConfiguration("d6464c70-3981-4db9-b2c5-5d08828ae686", DeviceType.CONTACT_SENSOR, "Linker deur"),
    "zigbee2mqtt/0x00158d0004852389" to DeviceMappingConfiguration("51e91085-5686-43e7-8c3d-871a73ee9b00", DeviceType.CONTACT_SENSOR, "Rechter deur"),
    "zigbee2mqtt/0x00158d00047d493d" to DeviceMappingConfiguration("99f9fb1b-fe98-414c-8562-68156ed3cc12", DeviceType.CONTACT_SENSOR, "Deur dakterras"),
//    "zigbee2mqtt/0x680ae2fffe724965" to DeviceMappingConfiguration("190f801d-2c55-487b-b44a-19ca61c432df", DeviceType.SWITCH, "Een switch (placeholder)"),
    "zigbee2mqtt/0x00158d00041193bb" to DeviceMappingConfiguration("505896d1-1b7a-4a58-9dbc-b28c39ddecfa", DeviceType.TEMPERATURE_SENSOR, "Verwarming")
)
val deviceIdToSensorIdMapping: Map<String, String> = devicesMappingConfigurations.map { it.value.deviceId to it.key }.toMap()

fun String.toDeviceName(): String? {
    val sensorId = deviceIdToSensorIdMapping[this]
    return devicesMappingConfigurations[sensorId]?.description
}

enum class DeviceType(val stateBody: KClass<out StateBody>) {
    CONTACT_SENSOR(ContactSensorStateBody::class),
    TEMPERATURE_SENSOR(TemperatureSensorStateBody::class),
    SWITCH(SwitchStateBody::class)
}
