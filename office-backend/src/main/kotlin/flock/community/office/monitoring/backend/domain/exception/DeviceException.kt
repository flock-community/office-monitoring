package flock.community.office.monitoring.backend.domain.exception

sealed class SensorIngestionException(override val message: String, cause: Throwable? = null) : Throwable(message, cause){

    data class UnknownDevice(val topic: String, val deviceMessage: String) : SensorIngestionException("Data from unmapped device received, topic: $topic, message: $deviceMessage")

}
