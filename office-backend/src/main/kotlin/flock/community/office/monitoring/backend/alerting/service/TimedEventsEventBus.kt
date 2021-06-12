package flock.community.office.monitoring.backend.alerting.service

import flock.community.office.monitoring.backend.alerting.domain.TimedUpdate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.*
import org.springframework.beans.factory.DisposableBean
import kotlin.math.absoluteValue

data class TimedUpdateRequest(
    val instant : Instant,
    val timedUpdate: TimedUpdate
)

@Component
class TimedEventsEventBus : DisposableBean {
    private val _events: MutableSharedFlow<TimedUpdate> = MutableSharedFlow(replay = 1)

    private val interval = Duration.ofSeconds(30)
    private val leeway = Duration.ofSeconds(5)
    private val scheduledEvents: MutableSet<TimedUpdateRequest> = mutableSetOf()

    private var job: Job

    init {
        runBlocking {
            job = launch {
                do {
                    scheduledEvents.map {
                        if ((it.instant.epochSecond - Instant.now().epochSecond).absoluteValue < leeway.toSeconds()) {
                            val tryEmit = _events.tryEmit(it.timedUpdate)
                            if (tryEmit){
                                scheduledEvents.remove(it)
                            }
                        }
                    }
                    delay(interval.toMillis())
                } while (true)
            }
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
        job.cancel("Stopping application")
    }


}
