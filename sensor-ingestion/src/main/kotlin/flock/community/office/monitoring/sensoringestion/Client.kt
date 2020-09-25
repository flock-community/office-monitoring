package flock.community.office.monitoring.sensoringestion

import org.eclipse.paho.client.mqttv3.*
import org.springframework.stereotype.Service


@Service
class PahoDemo : MqttCallback {


    init {
        doDemo()
    }


    var client: MqttClient? = null
    fun doDemo() {
        try {
            client = MqttClient("tcp://192.168.1.84:1883", "Sending")
            client!!.connect()
            client!!.setCallback(this)
            client!!.subscribe("zigbee2mqtt/#")
            val message = MqttMessage()
            message.payload = "A single message from my computer fff"
                    .toByteArray()
            client!!.publish("zigbee2mqtt", message)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    override fun connectionLost(cause: Throwable) {
        // TODO Auto-generated method stub
    }

    override fun messageArrived(topic: String, message: MqttMessage) {
        println(message)
    }

    override fun deliveryComplete(token: IMqttDeliveryToken) {
        // TODO Auto-generated method stub
    }
}