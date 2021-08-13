package flock.community.office.monitoring.mqtttoqueueconnector.queue

interface Publisher {
    fun publish(message: String)
}