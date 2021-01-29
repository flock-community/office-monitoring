package flock.community.office.monitoring.mqtttoqueueconnector

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MqttToQueueConnectorApplication

fun main(args: Array<String>) {
    runApplication<MqttToQueueConnectorApplication>(*args)
}
