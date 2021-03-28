package flock.community.office.monitoring.backend.controller

import flock.community.office.monitoring.backend.domain.service.CommandDispatcher
import flock.community.office.monitoring.utils.logging.loggerFor
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import org.springframework.stereotype.Service

@Service
class SubscriptionHandler(
    private val commandDispatcher: CommandDispatcher
) {

    private val logger = loggerFor<SubscriptionHandler>()

    // WIP: How about concurrency. Is handling 10 commands enough (per request)? Set upper limit?
    @OptIn(FlowPreview::class)
    suspend fun subscribeForCommands(commands: Flow<FlockMonitorCommandBody>): Flow<FlockMonitorMessage> = flow {
        val activeStreams: MutableMap<FlockMonitorCommandBody, Flow<FlockMonitorMessage>> = mutableMapOf()

        commands.map { command ->
            if (activeStreams.containsKey(command)) {
                logger.info("The $command commmand is already active")
                emptyFlow()
            } else {
                val commandFlow: Flow<FlockMonitorMessage> = commandDispatcher.dispatchCommand(command)
                activeStreams[command] = commandFlow

                commandFlow
            }
        }
            .flattenMerge(10)
            .collect {
                logger.info("Emitting FlockMonitorMessage: $it")
                emit(it)
            }
    }
}
