package flock.community.office.monitoring.backend.alerting

import flock.community.office.monitoring.backend.alerting.domain.Rule
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "alerting")
data class AlertingConfigurationProperties(
    val rules: List<Rule>,
    val signalAlertApi: SignalAlertApiConfig,
)

data class SignalAlertApiConfig (
    val enabled: Boolean,
    val host: String,
    val token: String,
    val phoneNumbers: List<String>
)
