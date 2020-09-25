package com.github.flockcommunity.officemonitoring.queueresolver.events

import java.time.Instant
import java.util.*

sealed class SensorEvent {

    abstract val id: UUID

    abstract val receivedDate: Instant

    data class DoorSensorEvent(
            override val id: UUID = UUID.randomUUID(),
            val battery: Int,
            val contact: Boolean,
            val linkQuality: Int,
            val voltage: Int,
            override val receivedDate: Instant= Instant.now()
    ) : SensorEvent()
}