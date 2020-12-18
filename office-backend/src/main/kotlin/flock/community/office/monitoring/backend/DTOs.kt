package flock.community.office.monitoring.backend

import java.time.ZonedDateTime


data class DeviceMessageWrapperDTO(
        val topic: String,
        val received: ZonedDateTime,
        val message: String
)
