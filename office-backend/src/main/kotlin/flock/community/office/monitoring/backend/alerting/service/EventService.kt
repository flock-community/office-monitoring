package flock.community.office.monitoring.backend.alerting.service

import flock.community.office.monitoring.backend.alerting.domain.Alert
import flock.community.office.monitoring.backend.alerting.domain.AlertChannel
import flock.community.office.monitoring.backend.alerting.domain.Event
import flock.community.office.monitoring.backend.alerting.domain.EventId
import flock.community.office.monitoring.backend.alerting.domain.EventState
import flock.community.office.monitoring.backend.alerting.domain.RuleId
import flock.community.office.monitoring.backend.alerting.domain.SentAlert
import flock.community.office.monitoring.backend.weather.domain.Coord
import flock.community.office.monitoring.backend.weather.domain.Main
import flock.community.office.monitoring.backend.weather.domain.Sys
import flock.community.office.monitoring.backend.weather.domain.WeatherPrediction
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Service
class EventService {

    fun getCurrentEvent(ruleId: RuleId): Event {
        val aRainPrediction = WeatherPrediction(
            null, null, null, Coord(1.2, 1.2), 1, 1, Main(1.2, 1, 1, 1.2, 1.2, 1.2), "",
            Sys("", 1, 1, 1, 1), 1, 1, emptyList(), null
        )
        return Event(
            id = EventId(UUID.randomUUID().toString()),
            ruleId = ruleId,
            state = EventState.ACTIVE,
            openedContactSensors = emptySet(),
            rainPrediction = aRainPrediction,
            lastStateChange = Instant.now(),
            sentAlerts = listOf(
                SentAlert(
                    Alert(
                        Duration.ofMinutes(10),
                        message = "Only some time left, watch out",
                        channel = AlertChannel.SIGNAL,
                        time = null
                    ),
                    dateTime = Instant.now()
                )
            )
        )
    }

    // Probably want this to be atomic or something to deal with race conditions?
    fun updateEvent(eventId: EventId, event : Event): Event {
        throw NotImplementedError("Still needs an impl")
    }
}
