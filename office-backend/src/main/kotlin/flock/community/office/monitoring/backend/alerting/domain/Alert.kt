package flock.community.office.monitoring.backend.alerting.domain

import java.time.Duration
import java.time.LocalTime


data class Alert(
    val timeToDeadline: Duration? = null,
    val time: LocalTime? = null,
    val message: String,
    val channel: AlertChannel
);

enum class AlertChannel {
    SIGNAL
}

