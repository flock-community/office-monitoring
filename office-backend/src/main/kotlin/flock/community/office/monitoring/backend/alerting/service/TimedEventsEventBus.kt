package flock.community.office.monitoring.backend.alerting.service

import flock.community.office.monitoring.backend.alerting.domain.TimedUpdate
import flock.community.office.monitoring.utils.logging.loggerFor
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.springframework.beans.factory.DisposableBean
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import kotlin.math.absoluteValue

data class TimedUpdateRequest(
    val instant: Instant,
    val timedUpdate: TimedUpdate
)

sealed class ScheduledEvents
class PublishEvent(val timedUpdateRequest: TimedUpdateRequest) : ScheduledEvents()
class PopEvents(val response: CompletableDeferred<Set<TimedUpdateRequest>>) : ScheduledEvents()

@Component
class TimedEventsEventBus : DisposableBean {
    private val _events: MutableSharedFlow<TimedUpdate> = MutableSharedFlow(replay = 1)
    private val scope = CoroutineScope(CoroutineName("TimedEventsEventBus"))
    private val scheduledEventsActor = scope.scheduledActor()

    private val interval = Duration.ofSeconds(10)
    private val leeway = Duration.ofSeconds(5)

    private val log = loggerFor<TimedEventsEventBus>()

    init {
        scope.launch {
            do {
                log.debug("Processing TimedUpdateRequests ....")
                val response = CompletableDeferred<Set<TimedUpdateRequest>>()
                scheduledEventsActor.send(PopEvents(response))
                val timedUpdateRequests: Set<TimedUpdateRequest> = response.await()

                timedUpdateRequests.forEach {
                    _events.emit(it.timedUpdate)
                }
                log.debug("Done Processing TimedUpdateRequests")
                delay(interval.toMillis())
            } while (true)
        }

    }

    suspend fun publish(timedUpdateRequest: TimedUpdateRequest): Boolean {
        scheduledEventsActor.send(PublishEvent(timedUpdateRequest))
        return true;
    }

    fun subscribe(): Flow<TimedUpdate> {
        return _events.asSharedFlow()
    }

    @OptIn(ObsoleteCoroutinesApi::class)
    private fun CoroutineScope.scheduledActor() = actor<ScheduledEvents> {
        val scheduledEvents: MutableSet<TimedUpdateRequest> = mutableSetOf()

        for (msg in channel) {
            when (msg) {
                is PublishEvent -> scheduledEvents.add(msg.timedUpdateRequest)
                is PopEvents -> {
                    val eventsToPopEvents: MutableSet<TimedUpdateRequest> = scheduledEvents.toMutableSet()
                    eventsToPopEvents.retainAll {
                        val secondsUntilEvent = (it.instant.epochSecond - Instant.now().epochSecond).absoluteValue
                        secondsUntilEvent < leeway.toSeconds()
                    }
                    msg.response.complete(eventsToPopEvents.toSet())
                    scheduledEvents.removeAll(eventsToPopEvents)
                }
            }
        }

    }

    override fun destroy() {
        log.info("Shutting down '${this::class.simpleName}'")
        scope.cancel("Stopping application")
    }


}
