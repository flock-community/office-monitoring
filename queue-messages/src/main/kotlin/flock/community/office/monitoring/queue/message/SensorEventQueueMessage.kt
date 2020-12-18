package flock.community.office.monitoring.queue.message

import java.io.Serializable
import java.time.Instant

data class SensorEventQueueMessage(
    val topic: String,
    val message: String,
    val received: Instant = Instant.now()
) : Serializable
