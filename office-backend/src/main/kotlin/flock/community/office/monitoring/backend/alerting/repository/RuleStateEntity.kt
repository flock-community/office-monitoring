package flock.community.office.monitoring.backend.alerting.repository

import org.springframework.cloud.gcp.data.datastore.core.mapping.Entity
import org.springframework.data.annotation.Id
import java.time.Instant

@Entity(name = "ruleState")
data class RuleStateEntity(
    @Id
    val id: String,
    val ruleId: String,
    val active: Boolean,
    val lastStateChange: Instant,
    val sentAlerts : List<SentAlertDto>,
)

@Entity
data class SentAlertDto(
    val alertId: String,
    val dateTime: Instant
)
