package com.github.flockcommunity.officemonitoring.queueresolver.mapping

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.flockcommunity.officemonitoring.queueresolver.events.SensorEvent
import com.github.flockcommunity.officemonitoring.queueresolver.mqtt.SensorInfo
import com.github.flockcommunity.officemonitoring.queueresolver.mqtt.SensorType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SensorEventMapper(
        val objectMapper: ObjectMapper,
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun map(sensorInfo: SensorInfo, json: String): SensorEvent = when (sensorInfo.type) {
        SensorType.DOOR -> objectMapper.readValue<SensorEvent.DoorSensorEvent>(json)
    }
}