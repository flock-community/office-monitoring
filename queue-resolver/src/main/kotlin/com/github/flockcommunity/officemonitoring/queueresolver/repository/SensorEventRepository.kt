package com.github.flockcommunity.officemonitoring.queueresolver.repository

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.flockcommunity.officemonitoring.queueresolver.events.SensorEvent
import org.springframework.stereotype.Repository
import java.util.*


@Repository
class SensorEventRepository {

    var cache: Cache<UUID, SensorEvent> = Caffeine.newBuilder()
            .maximumSize(10000)
            .build()

    fun save(sensorEvent: SensorEvent) {
        cache.put(sensorEvent.id, sensorEvent)
    }

    fun getAll(): List<SensorEvent> = cache.asMap().map { it.value }
}