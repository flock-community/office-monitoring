package flock.community.office.monitoring.backend.alerting.service

import flock.community.office.monitoring.backend.alerting.domain.TimedUpdate
import flock.community.office.monitoring.utils.logging.loggerFor
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import org.springframework.beans.factory.DisposableBean
import kotlin.math.absoluteValue

data class TimedUpdateRequest(
    val instant: Instant,
    val timedUpdate: TimedUpdate
)

@Component
class TimedEventsEventBus : DisposableBean {
    private val _events: MutableSharedFlow<TimedUpdate> = MutableSharedFlow(replay = 1)
    private val coroutineScope = CoroutineScope(CoroutineName("TimedEventsEventBus"))

    private val interval = Duration.ofSeconds(10)
    private val leeway = Duration.ofSeconds(5)
    private val scheduledEvents: MutableSet<TimedUpdateRequest> = mutableSetOf()

    private val log = loggerFor<TimedEventsEventBus>()

    init {
        coroutineScope.launch {
            do {
                log.debug("Processing TimedUpdateRequests ....")
                scheduledEvents.map {
                    if ((it.instant.epochSecond - Instant.now().epochSecond).absoluteValue < leeway.toSeconds()) {
                        val tryEmit = _events.tryEmit(it.timedUpdate)
                        if (tryEmit) {
                            scheduledEvents.remove(it)
                        } else {
                            log.warn("Processing scheduled event failed. Will try again in $interval. (${it.timedUpdate})")
                        }
                    }
                }
                log.debug("Done Processing TimedUpdateRequests")
                delay(interval.toMillis())
            } while (true)
        }

        coroutineScope.launch {
            delay(15_000L)
            log.info("Cancelling coroutinescope")
            coroutineScope.cancel("Cancelling")
        }

    }

    fun publish(timedUpdateRequest: TimedUpdateRequest): Boolean {
        scheduledEvents.add(timedUpdateRequest)
        return true;
    }

    fun subscribe(): Flow<TimedUpdate> {
        return _events.asSharedFlow()
    }

    override fun destroy() {
        log.info("Shutting down '${this::class.simpleName}'")
        coroutineScope.cancel("Stopping application")
    }


}
