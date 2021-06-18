package flock.community.office.monitoring.backend.alerting.service

import flock.community.office.monitoring.backend.alerting.domain.Event
import flock.community.office.monitoring.backend.alerting.domain.EventId
import flock.community.office.monitoring.backend.alerting.domain.EventState
import flock.community.office.monitoring.backend.alerting.domain.RuleId
import flock.community.office.monitoring.backend.weather.domain.WeatherForecast
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

//TODO: rewrite to connect with a datastore
@Service
class EventService {

    private var currentEvents: MutableMap<RuleId, Event> = mutableMapOf()


    // TODO: Deal with 'events' that have been idle for more than x minutes (alerts should be discarded)
    fun getCurrentEvent(ruleId: RuleId): Event {
        var event = currentEvents[ruleId]
        if (event == null) {
            event = createNewEvent(ruleId)
            currentEvents[ruleId] = event

        }

        return event;

    }

    fun createNewEvent(ruleId: RuleId): Event {
        val aRainPrediction = WeatherForecast(
            1.2, 1.2,
            "", 0, emptyList(), emptyList()
        )

        return Event(
            id = EventId(UUID.randomUUID().toString()),
            ruleId = ruleId,
            state = EventState.IDLE,
            openedContactSensors = emptySet(),
            rainForecast = aRainPrediction,
            lastStateChange = Instant.now(),
            sentAlerts = emptyList()
        )
    }

    // Probably want this to be atomic or something to deal with race conditions?
    fun updateEvent(eventId: EventId, event: Event) {
        currentEvents[event.ruleId] = event
    }
}
