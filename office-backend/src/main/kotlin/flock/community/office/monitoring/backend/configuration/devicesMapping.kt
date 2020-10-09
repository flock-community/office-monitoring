package flock.community.office.monitoring.backend.configuration


enum class DeviceType {
    CONTACT_SENSOR,
    TEMPERATURE_SENSOR,
    SWITCH;
}

data class InternalDevice(
        val id: String,
        val type: DeviceType
)

internal val iotDeviceIdToInternalId: Map<String, String> = mapOf(
        "zigbee2mqtt/0x00158d0004852389" to "c874bfd2-5f56-4e42-a456-a43c59c9b354",
        "zigbee2mqtt/0x00158d00041193bb" to "e392e8a6-e28a-4fa9-a935-a442e42ff781",
        "zigbee2mqtt/0x7cb03eaa0a093c6a" to "dd1be440-b277-4328-b3e0-a9c6185e08b4",
        "zigbee2mqtt/0x00158d0004852389" to "2b163ab7-2723-45cb-acba-9e2626d8c024",
        "zigbee2mqtt/0x00158d000578385c" to "bd955b1b-f89d-41eb-aef2-0eb11a1d8e20",
        "zigbee2mqtt/0x680ae2fffe724965" to "f32aae18-aa7a-45e5-89f0-7847e836b09b",
        "zigbee2mqtt/0x00158d00047d493d" to "21a9067e-55be-4ba4-9786-7839e7c10120"
)
