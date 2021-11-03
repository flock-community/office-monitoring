package flock.community.office.monitoring.backend.alerting.domain

data class RainAlertUpdate  (
    val contactSensorUpdate: ContactSensorUpdate? = null,
    val rainForecastUpdate: RainForecast? = null,
)
