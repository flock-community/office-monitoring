package flock.community.office.monitoring.backend

import com.fasterxml.jackson.databind.ObjectMapper
import flock.community.office.monitoring.backend.configuration.iotDeviceIdToInternalId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.time.ZonedDateTime

@Component
class UpdatesModel {

    private var _state = MutableStateFlow<DeviceMessageWrapperDTO>(DeviceMessageWrapperDTO("one,", ZonedDateTime.now(),"none"))
    val state: StateFlow<DeviceMessageWrapperDTO> get() = _state

    fun update(deviceMessage: DeviceMessageWrapperDTO) {
        _state.value = deviceMessage
    }
}


@Repository
class DevicesRepository(
        private val objectMapper: ObjectMapper,
        private val updatesModel: UpdatesModel

) {
    companion object {
        private val log = getLogger(this::class.java)
    }

    fun receiveMessage(payload: ByteArray) {

        val deviceMessage = objectMapper.readValue(payload, DeviceMessageWrapperDTO::class.java)

        GlobalScope.async {
            updatesModel.update(deviceMessage)
        }
    }
}