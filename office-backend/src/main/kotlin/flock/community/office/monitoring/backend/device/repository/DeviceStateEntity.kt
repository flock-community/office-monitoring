package flock.community.office.monitoring.backend.device.repository

import flock.community.office.monitoring.backend.device.configuration.DeviceType
import org.springframework.cloud.gcp.data.datastore.core.mapping.Entity
import org.springframework.data.annotation.Id
import java.time.Instant

@Entity(name = "deviceState")
class DeviceStateEntity(
    @Id
    val id: String,
    val type: DeviceType,
    val deviceId: String,
    val date: Instant,
    val state: String
)

