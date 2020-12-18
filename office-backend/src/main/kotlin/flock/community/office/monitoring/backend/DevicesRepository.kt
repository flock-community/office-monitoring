package flock.community.office.monitoring.backend

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.time.ZonedDateTime
import kotlin.random.Random

@ExperimentalCoroutinesApi
@Component
class UpdatesModel {

    private val log = getLogger(javaClass)
    private var _state = MutableStateFlow(DeviceMessageWrapperDTO("one,", ZonedDateTime.now(), "none"))
    val state: StateFlow<DeviceMessageWrapperDTO> get() = _state

    fun update(deviceMessage: DeviceMessageWrapperDTO) {
        log.info("Updating state with $deviceMessage")
        _state.value = deviceMessage
    }
}

@Repository
class DevicesRepository(
    private val objectMapper: ObjectMapper,
    private val deviceStateRepository: DeviceStateRepository,
    private val updatesModel: UpdatesModel

) {
    companion object {
        private val log = getLogger(this::class.java)
    }

    fun receiveMessage(payload: ByteArray) {

        try {
            val deviceMessage = objectMapper.readValue(payload, DeviceMessageWrapperDTO::class.java)

            deviceStateRepository.save(
                // Temporary, invalid mapping for demonstration purposes
                DeviceState(
                    eventId = Random.nextLong(),
                    id = deviceMessage.message,
                    iotDeviceId = deviceMessage.message,
                    state = deviceMessage.message
                )
            )


            GlobalScope.async {
                updatesModel.update(deviceMessage)
            }
        } catch (e: Exception) {
            log.error("Something went wrong trying to process message $payload", e)
        }
    }
}