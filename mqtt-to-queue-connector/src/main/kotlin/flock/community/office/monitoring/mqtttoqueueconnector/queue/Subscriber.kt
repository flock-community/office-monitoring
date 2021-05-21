package flock.community.office.monitoring.mqtttoqueueconnector.queue

interface Subscriber {
    fun subscribe(topic: String)
}