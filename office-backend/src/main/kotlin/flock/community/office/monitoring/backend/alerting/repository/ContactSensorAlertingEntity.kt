package flock.community.office.monitoring.backend.alerting.repository

import org.springframework.cloud.gcp.data.datastore.core.mapping.Entity
import org.springframework.data.annotation.Id
import java.time.Instant

@Entity(name = "contactSensorAlertingEntity")
data class ContactSensorAlertingEntity(
    @Id
    val id: String,
    val ruleId: String,
    val openedContactSensors: Set<String>,
    val lastStateChange: Instant,
)
