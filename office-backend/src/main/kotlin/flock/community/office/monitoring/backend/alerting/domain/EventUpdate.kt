package flock.community.office.monitoring.backend.alerting.domain

data class EventUpdate(
    val contactSensorUpdate: ContactSensorUpdate? = null,
    val timedUpdate: TimedUpdate? = null,
    val rainUpdate: RainUpdate? = null
)
