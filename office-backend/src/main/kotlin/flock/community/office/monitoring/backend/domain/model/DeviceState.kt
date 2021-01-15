package flock.community.office.monitoring.backend.domain.model

import flock.community.office.monitoring.backend.domain.service.DeviceType
import org.springframework.cloud.gcp.data.datastore.core.mapping.Entity
import org.springframework.data.annotation.Id
import java.time.Instant
import java.util.*

@Entity(name = "deviceState")
class DeviceState(
    @Id
    val id: String,
    val deviceType: DeviceType,
    val deviceId: String,
    val date: Instant,
    val state: String
)


