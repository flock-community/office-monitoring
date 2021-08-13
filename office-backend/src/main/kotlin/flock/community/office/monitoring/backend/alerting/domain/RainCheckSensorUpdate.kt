package flock.community.office.monitoring.backend.alerting.domain

data class RainCheckSensorUpdate(
    val contactSensorUpdate: ContactSensorUpdate? = null,
    val rainForecastUpdate: RainForecast? = null,
)
