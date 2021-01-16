package flock.community.office.monitoring.backend.domain.repository.entities

import flock.community.office.monitoring.backend.configuration.DeviceType
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

