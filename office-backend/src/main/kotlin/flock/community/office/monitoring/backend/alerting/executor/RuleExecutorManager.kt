package flock.community.office.monitoring.backend.alerting.executor

import flock.community.office.monitoring.backend.alerting.domain.Rule
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Component

@Component
class RuleExecutorManager(val executors: List<RuleImplExecutor<*>>) {

    fun start(rule: Rule): List<Flow<*>> {
        val type = rule.type
        val executor = executors.filter { it.type() == type }
        return executor.map {
            it.start(rule)
        }
    }
}
