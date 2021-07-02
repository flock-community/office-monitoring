package flock.community.office.monitoring.backend.alerting.repository

import org.springframework.cloud.gcp.data.datastore.repository.DatastoreRepository
import org.springframework.stereotype.Repository

@Repository
interface RuleStateRepository : DatastoreRepository<RuleStateEntity, String>{
    fun findAllByRuleIdAndActive(ruleId: String, isActive: Boolean = true) : Iterable<RuleStateEntity>
    fun findFirstByRuleIdAndActiveOrderByLastStateChangeDesc(ruleId: String, isActive: Boolean = true) : RuleStateEntity?
}
