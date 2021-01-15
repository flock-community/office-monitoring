package flock.community.office.monitoring.backend

import java.time.ZonedDateTime


data class DeviceMessageWrapperDTO(
    val topic: String,
    val received: ZonedDateTime,
    val message: String
)

sealed class GenericDeviceMessageDTO(
    open val last_seen: ZonedDateTime,
)

sealed class ACPoweredDeviceDTO(
    override val last_seen: ZonedDateTime
) : GenericDeviceMessageDTO(last_seen)

sealed class BatteryPoweredDeviceDTO(
    override val last_seen: ZonedDateTime,
    open val battery: Int,
    open val voltage: Int
) : GenericDeviceMessageDTO(last_seen)


data class ContactSensorMessageDTO(
    override val last_seen: ZonedDateTime,
    override val battery: Int,
    override val voltage: Int,
    val contact: Boolean?,
) : BatteryPoweredDeviceDTO(last_seen, battery, voltage)

data class TemperatureSensorMessageDTO(
    override val last_seen: ZonedDateTime,
    override val battery: Int,
    override val voltage: Int,
    val humidity: Double,
    val pressure: Int,
    val temperature: Double
) : BatteryPoweredDeviceDTO(last_seen, battery, voltage)


data class SwitchMessageDTO(
    override val last_seen: ZonedDateTime,
    val state: String,
) : ACPoweredDeviceDTO(last_seen)

