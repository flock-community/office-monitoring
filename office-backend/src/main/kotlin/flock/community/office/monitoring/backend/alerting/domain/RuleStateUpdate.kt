package flock.community.office.monitoring.backend.alerting.domain

import flock.community.office.monitoring.backend.alerting.service.TimedUpdateRequest

data class RuleStateUpdate(
    val contactSensorUpdate: ContactSensorUpdate? = null,
    val rainUpdate: RainForecast? = null,
    val timedUpdate: TimedUpdateRequest? = null
)
