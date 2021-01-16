package flock.community.office.monitoring.backend.repository

import flock.community.office.monitoring.backend.repository.model.DeviceStateEntity
import org.springframework.cloud.gcp.data.datastore.repository.DatastoreRepository
import org.springframework.stereotype.Repository

@Repository
interface DeviceStateRepository : DatastoreRepository<DeviceStateEntity, String>




