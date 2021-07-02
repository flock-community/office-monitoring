package flock.community.office.monitoring.backend.device.domain

import com.fasterxml.jackson.annotation.JsonProperty
import flock.community.office.monitoring.backend.device.configuration.DeviceType
import java.time.Instant

data class DeviceState<out STATE : StateBody>(
    val id: String,
    val type: DeviceType,
    val deviceId: String,
    val sensorId: String,
    val date: Instant,
    val state: STATE
)

interface StateBody {
    val lastSeen: Instant
}

interface BatteryDevice {
    val battery: Int
    val voltage: Int
}

data class ContactSensorStateBody(
    @JsonProperty("last_seen")
    override val lastSeen: Instant,
    override val battery: Int,
    override val voltage: Int,
    val contact: Boolean,
) : BatteryDevice, StateBody

data class TemperatureSensorStateBody(
    @JsonProperty("last_seen")
    override val lastSeen: Instant,
    override val battery: Int,
    override val voltage: Int,
    val humidity: Double,
    val pressure: Int,
    val temperature: Double
) : BatteryDevice, StateBody

data class SwitchStateBody(
    @JsonProperty("last_seen")
    override val lastSeen: Instant,
    val state: String
) : StateBody
