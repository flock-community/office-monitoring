package flock.community.office.monitoring.backend.alerting.executor

import flock.community.office.monitoring.backend.alerting.domain.ContactSensorUpdate
import flock.community.office.monitoring.backend.alerting.domain.Event
import flock.community.office.monitoring.backend.alerting.domain.EventState
import flock.community.office.monitoring.backend.alerting.domain.EventUpdate
import flock.community.office.monitoring.backend.alerting.domain.RainUpdate
import flock.community.office.monitoring.backend.alerting.domain.Rule
import flock.community.office.monitoring.backend.alerting.domain.RuleType
import flock.community.office.monitoring.backend.alerting.domain.TimedUpdate
import flock.community.office.monitoring.backend.alerting.service.AlertService
import flock.community.office.monitoring.backend.alerting.service.EventService
import flock.community.office.monitoring.backend.alerting.service.TimedEventsEventBus
import flock.community.office.monitoring.backend.alerting.service.TimedUpdateRequest
import flock.community.office.monitoring.backend.alerting.service.WeatherEventBus
import flock.community.office.monitoring.backend.device.DeviceStateEventBus
import flock.community.office.monitoring.backend.device.configuration.devicesMappingConfigurations
import flock.community.office.monitoring.backend.device.configuration.toDeviceName
import flock.community.office.monitoring.backend.device.domain.ContactSensorStateBody
import flock.community.office.monitoring.backend.device.domain.DeviceState
import flock.community.office.monitoring.backend.device.domain.StateBody
import flock.community.office.monitoring.backend.device.service.DeviceStateHistoryService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.stream.Collectors

@Service
class RainCheckSensorExecutor(
    val eventBus: DeviceStateEventBus,
    val historyService: DeviceStateHistoryService,
    val weatherEventBus: WeatherEventBus,
    val eventService: EventService,
    val timedEventsEventBus: TimedEventsEventBus,
    val alertService: AlertService
) : RuleImplExecutor<Event> {

    override fun type() = RuleType.RAIN_CHECK_CONTACT_SENSOR


    @OptIn(ExperimentalCoroutinesApi::class)
    override fun start(rule: Rule): Flow<Event> {
        // subscribe to updates / state changes
        val deviceStateUpdates = subscribeToDeviceStateUpdates(rule)
        val weatherUpdates = subscribeToWeatherUpdates()
        val timedUpdates = subscribeToTimedUpdates()

        return merge(deviceStateUpdates, weatherUpdates, timedUpdates)
            .map {
                // Handle updates
                val currentEvent = eventService.getCurrentEvent(rule.id)
                currentEvent
                    .let { e -> it.contactSensorUpdate?.handleContactSensorUpdate(e) ?: e }
                    .let { e -> it.rainUpdate?.handleWeatherUpdate(e) ?: e }
                    .let { e -> it.timedUpdate?.handleTimedUpdate(e) ?: e }
                    .also { e ->
                        if (e != currentEvent) {
                            eventService.updateEvent(e.id, e)

                            timedEventsEventBus.publish(
                                timedUpdateRequest = TimedUpdateRequest(
                                    Instant.now(),
                                    TimedUpdate("Trigger for alerting check")
                                )
                            )

                            // FIXME: Quick hack to report on changes in open/closed contactsensors
                            if (e.openedContactSensors != currentEvent.openedContactSensors) {
                                // send message
                                val alert = rule.alerts["every-contact-change"]

                                alert?.let {
                                    val properties = mapOf(
                                        "openContactSensors" to
                                                e.openedContactSensors.mapNotNull(String::toDeviceName).toString(),
                                        "allContactSensors" to
                                                rule.deviceIds.subtract(e.openedContactSensors)
                                                    .mapNotNull(String::toDeviceName).toString()
                                    )
                                    alertService.send(
                                        it,
                                        properties
                                    )
                                }
                            }
                        }
                    }
            }
    }


    private fun subscribeToTimedUpdates(): Flow<EventUpdate> = timedEventsEventBus.subscribe()
        .map { EventUpdate(timedUpdate = it) }


    private fun subscribeToWeatherUpdates(): Flow<EventUpdate> = weatherEventBus.subscribe()
        .map { EventUpdate(rainUpdate = RainUpdate("it might rain", it)) }


    private fun subscribeToDeviceStateUpdates(rule: Rule): Flow<EventUpdate> = flow {
        val sensorIds = rule.deviceIds.map { deviceId ->
            devicesMappingConfigurations.entries.first { it.value.deviceId == deviceId }.key
        }
        val latest: Flow<EventUpdate> = sensorIds
            .parallelStream()
            .map {
                historyService.getLatest(it)
            }
            .map { it.toContactSensorUpdate() }
            .collect(Collectors.toList())
            .filterNotNull()
            .map { EventUpdate(contactSensorUpdate = it) }
            .asFlow()

        emitAll(latest)

        eventBus.subscribe(null).filter { a -> sensorIds.contains(a.sensorId) }
            .mapNotNull { it.toContactSensorUpdate() }
            .map { EventUpdate(contactSensorUpdate = it) }
            .collect { emit(it) }

    }

    private fun DeviceState<StateBody>?.toContactSensorUpdate() =
        if (this != null && state is ContactSensorStateBody) {
            ContactSensorUpdate(
                deviceId = deviceId,
                date = state.lastSeen,
                contact = state.contact
            )
        } else {
            null
        }

    /**
     *
     * Updates the event based on contact state of a sensor (
     *      - any open door/window will render state 'active'
     *      - no doors/windows opened will render state 'idle'
     * If changes have occurred, a timedEvent will trigger (instant) to trigger alerting, if need be
     */
    private fun ContactSensorUpdate.handleContactSensorUpdate(event: Event): Event {

        val previousContactState = !event.openedContactSensors.contains(deviceId) // no contact means present in list
        // update event locally
        var newEvent: Event = event
        if (contact != previousContactState) {
            newEvent = when (contact) {
                true -> {
                    val openedContactSensors = event.openedContactSensors - deviceId
                    val newState = if (openedContactSensors.isNotEmpty()) EventState.ACTIVE else EventState.IDLE
                    event.copy(
                        state = newState,
                        openedContactSensors = openedContactSensors,
                        lastStateChange = Instant.now()
                    )
                }
                false -> event.copy(
                    state = EventState.ACTIVE,
                    openedContactSensors = event.openedContactSensors + deviceId,
                    lastStateChange = Instant.now()
                )
            }
        }

        // return new (updated) event
        return newEvent

    }

    private fun RainUpdate.handleWeatherUpdate(event: Event): Event {
        // TODO("Not yet implemented")
        return event
    }

    private fun TimedUpdate.handleTimedUpdate(event: Event): Event {
        // TODO("Not yet implemented")
        return event
    }
}

