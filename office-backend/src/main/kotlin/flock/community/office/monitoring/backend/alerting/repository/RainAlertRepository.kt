package flock.community.office.monitoring.backend.alerting.repository

import org.springframework.cloud.gcp.data.datastore.repository.DatastoreRepository
import org.springframework.stereotype.Repository

@Repository
interface RainAlertRepository : DatastoreRepository<RainAlertEntity, String> {
    fun findByRuleId(ruleId: String): RainAlertEntity?
}

