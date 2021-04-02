package flock.community.office.monitoring.backend.domain.repository

import flock.community.office.monitoring.backend.domain.repository.entities.DeviceStateEntity
import org.springframework.cloud.gcp.data.datastore.repository.DatastoreRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface DeviceStateRepository : DatastoreRepository<DeviceStateEntity, String>{
    fun findTop100ByDeviceIdOrderByDateDesc(deviceId: String): Iterable<DeviceStateEntity>
    fun findAllByDeviceIdAndDateGreaterThanOrderByDateAsc(deviceId: String, from: Instant): Iterable<DeviceStateEntity>
}




