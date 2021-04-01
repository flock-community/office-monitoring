package flock.community.office.monitoring.backend.domain.model

import com.fasterxml.jackson.annotation.JsonProperty
import flock.community.office.monitoring.backend.configuration.DeviceType
import java.time.Instant

data class DeviceState<out STATE : StateBody>(
    val id: String,
    val type: DeviceType,
    val deviceId: String,
    val date: Instant,
    val state: STATE
) {

    fun <T : StateBody> equalsWithoutDateAndId(its2: DeviceState<T>): Boolean {
        if (this::class != its2::class) {
            return false
        }

        return this.type == its2.type &&
                this.deviceId == its2.deviceId &&
                this.state.equalsWithoutLastSeen(its2.state)
    }
}

interface StateBody {
    val lastSeen: Instant

    fun equalsWithoutLastSeen(other: StateBody): Boolean
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
) : BatteryDevice, StateBody {
    override fun equalsWithoutLastSeen(other: StateBody): Boolean {
        if (other !is ContactSensorStateBody) {
            return false
        }
        return battery == other.battery &&
                voltage == other.voltage &&
                contact == other.contact
    }

    override fun toString(): String {
        return "ContactSensor[battery: $battery, voltage: $voltage, contact: $contact]"
    }
}

data class TemperatureSensorStateBody(
    override val lastSeen: Instant,
    override val battery: Int,
    override val voltage: Int,
    val humidity: Double,
    val pressure: Int,
    val temperature: Double
) : BatteryDevice, StateBody {
    override fun equalsWithoutLastSeen(other: StateBody): Boolean {
        if (other !is TemperatureSensorStateBody) {
            return false
        }
        return battery == other.battery &&
                voltage == other.voltage &&
                humidity == other.humidity &&
                pressure == other.pressure &&
                temperature == other.temperature
    }
}

data class SwitchStateBody(
    override val lastSeen: Instant,
    val state: String
) : StateBody {
    override fun equalsWithoutLastSeen(other: StateBody): Boolean {
        if (other !is SwitchStateBody) {
            return false
        }
        return state == other.state
    }
}
