package flock.community.office.monitoring.backend

import org.springframework.cloud.gcp.data.datastore.core.mapping.Entity
import org.springframework.cloud.gcp.data.datastore.repository.DatastoreRepository
import org.springframework.data.annotation.Id

interface DeviceStateRepository : DatastoreRepository<DeviceState, String>

@Entity(name = "deviceState")
data class DeviceState(
        @Id
        val eventId: Long? = null,
        val id: String,
        val iotDeviceId: String,
        val state: String //JSON blob of device state
)