package flock.community.office.monitoring.backend.domain.repository

import flock.community.office.monitoring.backend.domain.model.DeviceState
import org.springframework.cloud.gcp.data.datastore.repository.DatastoreRepository
import org.springframework.stereotype.Repository

@Repository
interface DeviceStateRepository : DatastoreRepository<DeviceState, String>




