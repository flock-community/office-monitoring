package flock.community.office.monitoring.backend.alerting.domain

import java.time.Instant

data class ContactSensorUpdate(
    val deviceId: String,
    val date: Instant,
    val contact: Boolean
)
