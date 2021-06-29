package flock.community.office.monitoring.backend.alerting.service

import flock.community.office.monitoring.backend.alerting.AlertingConfigurationProperties
import flock.community.office.monitoring.backend.alerting.domain.Rule
import flock.community.office.monitoring.backend.alerting.executor.RuleExecutorManager
import flock.community.office.monitoring.utils.logging.loggerFor
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import org.springframework.beans.factory.DisposableBean
import org.springframework.stereotype.Service

@ExperimentalCoroutinesApi
@Service
class AlertingService(
    alertingConfiguration: AlertingConfigurationProperties,
    val ruleExecutorManager: RuleExecutorManager
) : DisposableBean {

    private val scope = CoroutineScope(CoroutineName("AlertingService"))
    private val log = loggerFor<AlertingService>()

    init {
        scope.launch {
            delay(5000) // TODO Wait until application is booted
            alertingConfiguration.rules
                .forEach { monitorRule(it) }
        }
    }

    private fun CoroutineScope.monitorRule(it: Rule): Job = launch(CoroutineName("Alert #${it.id.value}")) {
        // Launch each rule in its own coroutine
        ruleExecutorManager.start(it)
            .merge()
            .collect()
    }

    override fun destroy() {
        log.info("Shutting down '${this::class.simpleName}'")
        scope.cancel("Stopping application")
    }
}
