package flock.community.office.monitoring.backend.alerting.service

import flock.community.office.monitoring.backend.alerting.AlertingConfigurationProperties
import flock.community.office.monitoring.backend.alerting.domain.Rule
import flock.community.office.monitoring.backend.alerting.domain.RuleId
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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.springframework.beans.factory.DisposableBean
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

data class TimedUpdateRequest(
    val dateTime: Instant,
    val ruleId: RuleId,
    val triggerReason: String
)

sealed class ScheduledEvents
class PublishEvent(val timedUpdateRequest: TimedUpdateRequest) : ScheduledEvents()
class PopEvents(val response: CompletableDeferred<Set<TimedUpdateRequest>>) : ScheduledEvents()

@Component
class TimedUpdatesEventBus(
    private val alertingConfigurationProperties: AlertingConfigurationProperties
) : DisposableBean {
    private val _events: MutableSharedFlow<TimedUpdateRequest> = MutableSharedFlow(replay = 1)
    private val scope = CoroutineScope(CoroutineName("TimedEventsEventBus"))
    private val scheduledEventsActor = scope.scheduledActor()

    private val evaluateInterval = Duration.ofSeconds(10) // TODO: move to configuration
    private val periodicTimeUpdateRequestInterval = Duration.ofMinutes(5) // TODO: move to configuration

    private val log = loggerFor<TimedUpdatesEventBus>()

    init {
        setupPeriodicTimedUpdateRequests(periodicTimeUpdateRequestInterval)
        setupRegularEvaluation(evaluateInterval)
    }

    private fun setupRegularEvaluation(evaluateInterval: Duration) {
        scope.launch {
            do {
//                log.debug("Processing TimedUpdateRequests ....")
                val response = CompletableDeferred<Set<TimedUpdateRequest>>()
                scheduledEventsActor.send(PopEvents(response))
                val timedUpdateRequests: Set<TimedUpdateRequest> = response.await()

                if (timedUpdateRequests.isNotEmpty()) {
                    val groupBy: Map<RuleId, List<TimedUpdateRequest>> = timedUpdateRequests.groupBy { it.ruleId }
                    groupBy.entries.forEach {
                        val reasons = it.value.map { a -> a.triggerReason }

                        _events.emit(TimedUpdateRequest(Instant.now(), it.key, reasons.toString()))
                    }
                }
//                log.debug("Done Processing TimedUpdateRequests")
                delay(evaluateInterval.toMillis())
            } while (true)
        }
    }

    private fun setupPeriodicTimedUpdateRequests(interval: Duration) {
        scope.launch(CoroutineName("RegularTimedUpdate")) {
            do {
                alertingConfigurationProperties.rules.forEach {
                    publish(TimedUpdateRequest(Instant.now(), it.id, "Trigger to regularly check for alerting"))
                }
                delay(interval.toMillis())
            } while (true)
        }
    }

    suspend fun publish(timedUpdateRequest: TimedUpdateRequest): Boolean {
        if (timedUpdateRequest.dateTime > Instant.now().plus(Duration.ofHours(6))) {
            log.info("Ignoring TimeUpdateRequest more than 6 hours in the future ($timedUpdateRequest}")
            return false
        }

        scheduledEventsActor.send(PublishEvent(timedUpdateRequest))
        return true;
    }

    fun subscribe(rule: Rule): Flow<TimedUpdateRequest> {
        return _events.asSharedFlow().filter { it.ruleId == rule.id }
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
                        val durationUntilEvent = Duration.between(Instant.now(), it.dateTime)
                        durationUntilEvent < evaluateInterval.dividedBy(2)
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
