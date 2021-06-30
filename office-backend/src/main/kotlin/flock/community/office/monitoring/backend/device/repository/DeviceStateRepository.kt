package flock.community.office.monitoring.backend.device.repository

import org.springframework.cloud.gcp.data.datastore.repository.DatastoreRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface DeviceStateRepository : DatastoreRepository<DeviceStateEntity, String>{
    fun findTop100ByDeviceIdOrderByDateDesc(deviceId: String): Iterable<DeviceStateEntity>
    fun findAllByDeviceIdAndDateGreaterThanOrderByDateAsc(deviceId: String, from: Instant): Iterable<DeviceStateEntity>
}




