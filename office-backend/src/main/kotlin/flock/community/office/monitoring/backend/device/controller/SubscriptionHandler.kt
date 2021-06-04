package flock.community.office.monitoring.backend.device.controller

import flock.community.office.monitoring.backend.device.controller.dto.FlockMonitorCommandBody
import flock.community.office.monitoring.backend.device.controller.dto.FlockMonitorMessage
import flock.community.office.monitoring.utils.logging.loggerFor
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class SubscriptionHandler(
    private val commandDispatcher: CommandDispatcher
) {

    private val logger = loggerFor<SubscriptionHandler>()

    // WIP: How about concurrency. Is handling 10 commands enough (per request)? Set upper limit?
    @OptIn(FlowPreview::class)
    suspend fun subscribeForCommands(commands: Flow<FlockMonitorCommandBody>, requestId: UUID): Flow<FlockMonitorMessage> = flow {
        val activeStreams: MutableMap<String, Flow<FlockMonitorMessage>> = mutableMapOf()

        commands.map { command ->
            val key = command.toKey().also{logger.debug("The command key: $it")}
            if (activeStreams.containsKey(key)) {
                logger.debug("The $command command (key $key) is already active for request $requestId")
                emptyFlow()
            } else {
                val commandFlow: Flow<FlockMonitorMessage> = commandDispatcher.dispatchCommand(command)
                activeStreams[key] = commandFlow

                commandFlow
            }
        }
            .flattenMerge(10)
            .collect {
                logger.info("Emitting to $requestId FlockMonitorMessage: $it ")
                emit(it)
            }
    }
}

/** Ugly hack to exclude epoch from intant string **/
private fun FlockMonitorCommandBody.toKey(): String {
    return when(this){
        is FlockMonitorCommandBody.GetDevicesCommand -> this::class.java.toString()
        is FlockMonitorCommandBody.GetDeviceStateCommand -> this.copy(from = Instant.EPOCH).toString()
    }
}
