package flock.community.office.monitoring.backend.alerting.service

import flock.community.office.monitoring.backend.alerting.domain.Rule
import flock.community.office.monitoring.backend.alerting.executor.RuleExecutorManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.DisposableBean
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.stereotype.Service

@ConstructorBinding
@ConfigurationProperties(prefix = "alerting")
data class AlertingConfiguration(
    val rules: List<Rule>,
)

@ExperimentalCoroutinesApi
@Service
class AlertingService(
    alertingConfiguration: AlertingConfiguration,
    val ruleExecutorManager: RuleExecutorManager
) : DisposableBean {

    private var job: Job

    init {
        runBlocking {
            job = launch {
                alertingConfiguration.rules
                    .flatMap { ruleExecutorManager.start(it) }
                    .merge()
                    .collect()

            }
        }
    }

    override fun destroy() {
        job.cancel("Stopping application")
    }
}
