package flock.community.office.monitoring.mqtttoqueueconnector.loggable

import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface Loggable {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}