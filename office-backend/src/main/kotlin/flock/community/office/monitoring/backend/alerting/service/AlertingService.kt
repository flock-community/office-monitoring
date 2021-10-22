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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import org.springframework.beans.factory.DisposableBean
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono.delay
import java.time.Duration

@Service
class AlertingService(
    private val alertingConfiguration: AlertingConfigurationProperties,
    private val ruleExecutorManager: RuleExecutorManager,
    private val alertSenderService: AlertSenderService
) : DisposableBean {

    private val scope = CoroutineScope(CoroutineName("AlertingService"))
    private val log = loggerFor<AlertingService>()

    fun start() {
        scope.launch {
            log.info("Starting AlertingService in 10 seconds")
            delay(Duration.ofSeconds(10))
            alertingConfiguration.rules
                .forEach { monitorRule(it) }
        }
    }

    private fun CoroutineScope.monitorRule(it: Rule): Job = launch(CoroutineName("Alert #${it.id.value}")) {
        // Launch each rule in its own coroutine
        try {
            ruleExecutorManager.start(it)
                .merge()
                .collect()
        } catch (e: Exception) {
            val alertMessage = e.message ?: "Unknown error occurred while starting alerting for rule ${it.name}"
            alertSenderService.send("$alertMessage. Rule is NOT monitored (NOTE: no notification if error is fixed)", it.cancelMessage.channel)

            log.error(alertMessage, e);
        }
    }

    override fun destroy() {
        log.info("Shutting down '${this::class.simpleName}'")
        scope.cancel("Stopping application")
    }
}
