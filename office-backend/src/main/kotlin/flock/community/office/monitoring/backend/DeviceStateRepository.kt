package flock.community.office.monitoring.backend

import flock.community.office.monitoring.backend.configuration.DeviceType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.slf4j.LoggerFactory
import org.springframework.cloud.gcp.data.datastore.core.mapping.Entity
import org.springframework.cloud.gcp.data.datastore.repository.DatastoreRepository
import org.springframework.data.annotation.Id
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.time.ZonedDateTime

@Repository
interface DeviceStateRepository : DatastoreRepository<DeviceState, String>{
}

@Entity(name = "deviceState")
data class DeviceState(
    @Id
    val eventId: Long? = -1,
    val id: String,
//    val iotDeviceId: String,
    val type : DeviceType,//Enum
    val timeStamp: ZonedDateTime,
    val state: Any //JSON blob of device state (TempSensor || DoorSensor)
)

@ExperimentalCoroutinesApi
@Component
class DeviceStateHistoryService(private val repository: DeviceStateRepository) {
    private val log = LoggerFactory.getLogger(javaClass)

    // Apply filtering
    // Time
    // deviceType
    // deviceId
    fun getHistory(): List<DeviceState> {
        return try {repository.findAll().take(10)} catch (e: Exception){
            log.warn("Corrupt DeviceState: ${e.message}", e)
            emptyList()
        }
    }
}
