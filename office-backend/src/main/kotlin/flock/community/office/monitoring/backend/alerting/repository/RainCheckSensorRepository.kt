package flock.community.office.monitoring.backend.alerting.repository

import org.springframework.cloud.gcp.data.datastore.repository.DatastoreRepository
import org.springframework.stereotype.Repository

@Repository
interface RainCheckSensorRepository : DatastoreRepository<RainCheckSensorEntity, String> {
    fun findByRuleId(ruleId: String): RainCheckSensorEntity?
}

