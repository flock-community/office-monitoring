package flock.community.office.monitoring.backend.device.controller

import flock.community.office.monitoring.backend.device.controller.dto.FlockMonitorCommandBody
import flock.community.office.monitoring.backend.device.controller.dto.FlockMonitorMessage
import flock.community.office.monitoring.utils.logging.loggerFor
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import org.springframework.stereotype.Service
import java.util.*

@Service
class SubscriptionHandler(
    private val commandDispatcher: CommandDispatcher
) {
    private val logger = loggerFor<SubscriptionHandler>()

    @OptIn(FlowPreview::class)
    suspend fun subscribeForCommands(commands: Flow<FlockMonitorCommandBody>, requestId: UUID): Flow<FlockMonitorMessage> = flow {
        val activeStreams: MutableMap<String, Flow<FlockMonitorMessage>> = mutableMapOf()

        commands.subscribeAndDispatch(activeStreams)
            .flattenMerge(10)
            .collect {
                logger.info("Emitting to $requestId FlockMonitorMessage: $it ")
                emit(it)
            }
    }

    private fun Flow<FlockMonitorCommandBody>.subscribeAndDispatch(activeStreams: MutableMap<String, Flow<FlockMonitorMessage>>) = map { command ->
        command.dispatch(activeStreams).also {
            logger.debug("The command key: ${command.key}")
        }
    }

    private fun FlockMonitorCommandBody.dispatch(activeStreams: MutableMap<String, Flow<FlockMonitorMessage>>): Flow<FlockMonitorMessage> = when (activeStreams.containsKey(key)) {
        true -> emptyFlow()
        false -> commandDispatcher.dispatchCommand(this).also {
            activeStreams[key] = it
        }
    }
}
