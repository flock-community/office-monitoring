package flock.community.office.monitoring.backend.alerting.executor

import flock.community.office.monitoring.backend.alerting.domain.Rule
import flock.community.office.monitoring.backend.alerting.domain.RuleType
import kotlinx.coroutines.flow.Flow

interface RuleImplExecutor<T> {
    fun type(): RuleType
    fun start(rule: Rule) : Flow<T>
}
