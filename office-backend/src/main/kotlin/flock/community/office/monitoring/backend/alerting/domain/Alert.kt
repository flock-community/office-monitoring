package flock.community.office.monitoring.backend.alerting.domain

import java.time.Duration
import java.time.LocalTime


data class Alert(
    val timeToDeadline: Duration?,
    val time: LocalTime?,
    val message: String,
    val channel: AlertChannel
);

enum class AlertChannel {
    SIGNAL
}

