package flock.community.office.monitoring.backend.domain.repository

import flock.community.office.monitoring.backend.domain.repository.entities.DeviceStateEntity
import org.springframework.cloud.gcp.data.datastore.repository.DatastoreRepository
import org.springframework.stereotype.Repository

@Repository
interface DeviceStateRepository : DatastoreRepository<DeviceStateEntity, String>{
    fun findFirst10ByDeviceIdIsNotIn(deviceId: String) : Iterable<DeviceStateEntity>
    fun findTop500ByDeviceIdOrderByDateDesc(deviceId: String): Iterable<DeviceStateEntity>
    fun findTop10ByDeviceId(deviceId: String): Iterable<DeviceStateEntity>
    fun findDeviceStateEntitiesByDeviceId(deviceId: String):  Iterable<DeviceStateEntity>
}




