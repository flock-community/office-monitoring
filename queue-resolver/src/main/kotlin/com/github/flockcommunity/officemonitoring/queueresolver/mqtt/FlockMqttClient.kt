package com.github.flockcommunity.officemonitoring.queueresolver.mqtt

import org.eclipse.paho.client.mqttv3.MqttClient
import org.springframework.stereotype.Component

@Component
class FlockMqttClient(
        val flockMqttCallback: FlockMqttCallback
) {

    init {
        MqttClient("tcp://192.168.1.84:1883", MqttClient.generateClientId())
                .apply {
                    setCallback(flockMqttCallback)
                }.run {
                    this.connect()
                    this.subscribe("zigbee2mqtt/#")
                }

    }
}