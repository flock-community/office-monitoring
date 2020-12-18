package flock.community.office.monitoring.backend

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.slf4j.LoggerFactory
import org.springframework.cloud.gcp.data.datastore.core.mapping.Entity
import org.springframework.cloud.gcp.data.datastore.repository.DatastoreRepository
import org.springframework.data.annotation.Id
import org.springframework.stereotype.Component
import java.time.ZonedDateTime

interface DeviceStateRepository : DatastoreRepository<DeviceState, String>

@Entity(name = "deviceState")
data class DeviceState(
    @Id
    val eventId: Long? = null,
    val id: String,
    val iotDeviceId: String,
    val state: String //JSON blob of device state
)


@ExperimentalCoroutinesApi
@Component
class DeviceStateHistoryService(private val repository: DeviceStateRepository){


    private val log = LoggerFactory.getLogger(javaClass)

    fun getHistory(): List<DeviceMessageWrapperDTO> {
        val findAll: MutableIterable<DeviceState> = repository.findAll()
        return findAll.take(10).map{
            DeviceMessageWrapperDTO("history", ZonedDateTime.now(), it.state)
        }
    }

}