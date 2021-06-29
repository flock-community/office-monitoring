package flock.community.office.monitoring.backend.alerting.executor

import flock.community.office.monitoring.backend.alerting.domain.Alert
import flock.community.office.monitoring.backend.alerting.domain.AlertConfig
import flock.community.office.monitoring.backend.alerting.domain.AlertId
import flock.community.office.monitoring.backend.alerting.domain.ContactSensorUpdate
import flock.community.office.monitoring.backend.alerting.domain.HourlyRainForecast
import flock.community.office.monitoring.backend.alerting.domain.RainForecast
import flock.community.office.monitoring.backend.alerting.domain.Rule
import flock.community.office.monitoring.backend.alerting.domain.RuleState
import flock.community.office.monitoring.backend.alerting.domain.RuleStateUpdate
import flock.community.office.monitoring.backend.alerting.domain.RuleType
import flock.community.office.monitoring.backend.alerting.domain.SentAlert
import flock.community.office.monitoring.backend.alerting.domain.containsRainForecast
import flock.community.office.monitoring.backend.alerting.domain.toAlertId
import flock.community.office.monitoring.backend.alerting.service.AlertSenderService
import flock.community.office.monitoring.backend.alerting.service.RuleStateService
import flock.community.office.monitoring.backend.alerting.service.TimedEventsEventBus
import flock.community.office.monitoring.backend.alerting.service.TimedUpdateRequest
import flock.community.office.monitoring.backend.device.DeviceStateEventBus
import flock.community.office.monitoring.backend.device.configuration.devicesMappingConfigurations
import flock.community.office.monitoring.backend.device.configuration.toDeviceName
import flock.community.office.monitoring.backend.device.domain.ContactSensorStateBody
import flock.community.office.monitoring.backend.device.domain.DeviceState
import flock.community.office.monitoring.backend.device.domain.StateBody
import flock.community.office.monitoring.backend.device.service.DeviceStateHistoryService
import flock.community.office.monitoring.backend.weather.WeatherEventBus
import flock.community.office.monitoring.utils.logging.loggerFor
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
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.util.stream.Collectors


@Service
class RainCheckSensorExecutor(
    val deviceStateEventBus: DeviceStateEventBus,
    val weatherEventBus: WeatherEventBus,
    val timedEventsEventBus: TimedEventsEventBus,
    val historyService: DeviceStateHistoryService,
    val ruleStateService: RuleStateService,
    val alertSenderService: AlertSenderService,
    @Qualifier("alerts") val alerts: Map<AlertId, Alert>
) : RuleImplExecutor<RuleState> {

    override fun type() = RuleType.RAIN_CHECK_CONTACT_SENSOR

    private val log = loggerFor<RuleImplExecutor<RuleState>>()

    private inline fun <T> guardAll(block: () -> T): T? = try {
        block()
    } catch (t: Throwable) {
        log.error("Error occurred clearing state change handling(s) for. This means alerting might run out of sync(!!)", t)
        // TODO: Trigger a 'reset' after x time (maybe exponential backoff), to do a full restart?
        null
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun start(rule: Rule): Flow<RuleState> {
        // subscribe to updates / state changes
        val deviceStateUpdates = subscribeToDeviceStateUpdates(rule)
        val weatherUpdates = subscribeToWeatherUpdates()
        val timedUpdates = subscribeToTimedUpdates()

        return merge(deviceStateUpdates, weatherUpdates, timedUpdates)
            .mapNotNull { ruleStateUpdate ->
                guardAll {
                    val currentRuleState = ruleStateService.getActiveRuleState(rule.id)
                    currentRuleState
                        .let { ruleStateUpdate.contactSensorUpdate?.handleContactSensorUpdate(it) ?: it }
                        .let { ruleStateUpdate.rainUpdate?.handleWeatherUpdate(it) ?: it }
                        .let { ruleStateUpdate.timedUpdate?.handleTimedUpdate(it, rule) ?: it }
                        .also {
                            if (it != currentRuleState) {
                                ruleStateService.update(it)

                                timedEventsEventBus.publish(
                                    timedUpdateRequest = TimedUpdateRequest(
                                        Instant.now(),
                                        "RuleState has changed."
                                    )
                                )
                            }
                        }
                }
            }
    }


    private fun subscribeToTimedUpdates(): Flow<RuleStateUpdate> = timedEventsEventBus.subscribe()
        .map { RuleStateUpdate(timedUpdate = it) }


    private fun subscribeToWeatherUpdates(): Flow<RuleStateUpdate> = weatherEventBus.subscribe()
        .map { RuleStateUpdate(rainUpdate = it.toRainUpdate()) }


    private fun subscribeToDeviceStateUpdates(rule: Rule): Flow<RuleStateUpdate> = flow {
        val sensorIds = rule.deviceIds.map { deviceId ->
            devicesMappingConfigurations.entries.first { it.value.deviceId == deviceId }.key
        }
        val latest: Flow<RuleStateUpdate> = sensorIds
            .parallelStream()
            .map {
                historyService.getLatest(it)
            }
            .map { it.toContactSensorUpdate() }
            .collect(Collectors.toList())
            .filterNotNull()
            .map { RuleStateUpdate(contactSensorUpdate = it) }
            .asFlow()

        emitAll(latest)

        deviceStateEventBus.subscribe(null).filter { a -> sensorIds.contains(a.sensorId) }
            .mapNotNull { it.toContactSensorUpdate() }
            .map { RuleStateUpdate(contactSensorUpdate = it) }
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
     * Updates the event based on contact state of a sensor
     *      -  Keeps track of open contact sensors
     */
    private fun ContactSensorUpdate.handleContactSensorUpdate(ruleState: RuleState): RuleState {

        val previousContactState =
            !ruleState.openedContactSensors.contains(deviceId) // no contact means present in list
        // update event locally
        var newRuleState: RuleState = ruleState
        if (contact != previousContactState) {
            newRuleState = when (contact) {
                true -> {
                    ruleState.copy(
                        openedContactSensors = ruleState.openedContactSensors - deviceId,
                        lastStateChange = Instant.now()
                    )
                }
                false -> ruleState.copy(
                    openedContactSensors = ruleState.openedContactSensors + deviceId,
                    lastStateChange = Instant.now()
                )
            }
        }

        // return new (updated) event
        return newRuleState

    }

    private fun RainForecast.handleWeatherUpdate(ruleState: RuleState): RuleState {
        val firstHourlyForecastWithRain = this.getFirstHourlyForecastWithRain(0.2, 0.7)

        // Check whether the previous rain forecast is still applicable
        if (ruleState.rainForecast != null) {
            val newForecastAtLastPrediction =
                this.hourlyForecast.find { it.dateTime == ruleState.rainForecast.dateTime }

            // check whether there's still rain predicted at the previously predicted time (Tn-1):
            //  - will it still rain at Tn-1?
            //  - is the previously predicted time (Tn-1) earlier than the currently predicted time (Tn)
            if (newForecastAtLastPrediction.containsRainForecast(0.1, 0.5)
                && (firstHourlyForecastWithRain == null || ruleState.rainForecast.dateTime < firstHourlyForecastWithRain.dateTime)
            ) {
                // previous prediction is leading
                return ruleState.copy(rainForecast = newForecastAtLastPrediction)
            }
        }
        return ruleState.copy(rainForecast = firstHourlyForecastWithRain)
    }

    private suspend fun TimedUpdateRequest.handleTimedUpdate(ruleState: RuleState, rule: Rule): RuleState {

        log.info("Resolving timedUpdate because: $triggerReason")

        val x: List<SentAlert> = listOfNotNull(
            // FIXME: Quick hack to report on changes in open/closed contact sensors
            trySendContactSensorChangeAlert(ruleState, rule),
            trySendWeatherUpdateAlert(ruleState, rule)
        )

        if (x.isNotEmpty()) {
            return ruleState.copy(
                sentAlerts = ruleState.sentAlerts + x
            )
        }

        return ruleState
    }

    private suspend fun trySendWeatherUpdateAlert(ruleState: RuleState, rule: Rule): SentAlert? {
        // TODO: Determine if fixed time alerting is necessary (e.g. every day at 5pm? latest state)
        // Out of scope?
        val sentAlerts = rule.alerts.mapNotNull {
            // Sent message at configured time
            val time = it.value.time
            if (time != null) {
                val currentTime = LocalTime.from(Instant.now().atZone(ZoneId.of("Europe/Paris")))
                if (Duration.between(currentTime, time).abs().seconds < 30) {
                    log.info("Within time window to send Alert ${it.key}.")
                    val properties = getAlertProperties(ruleState, rule)
                    alertSenderService.send(it.value, properties)

                    SentAlert(ruleState.openedContactSensors, it.toAlertId(rule), Instant.now())
                }
            }
        }

        // Check if doors are open
        if (ruleState.openedContactSensors.isEmpty()) {
            //  no doors open

            // check if 'cancel' message is needed
            if (ruleState.sentAlerts.isNotEmpty()) {
                // TODO: Are cancel messages needed for POC?
                // send cancel message

                // clear event
                ruleStateService.clearByRuleId(rule.id)
            }

        } else if (ruleState.rainForecast == null || Duration.between(
                Instant.now(),
                ruleState.rainForecast.dateTime
            ) < rule.alertingWindow
        ) {
            // check if 'cancel' message is needed
            if (ruleState.sentAlerts.isNotEmpty()) {
                // TODO: Are cancel messages needed for POC?
                // send cancel message

                // clear event
                ruleStateService.clearByRuleId(rule.id)
            }
        } else {
            //  doors open -->
            //      check which alert has been sent (check latest alert (of this type)
            val latestSentAlert: SentAlert? =
                ruleState.sentAlerts.filter { alerts[it.alertId]?.timeToDeadline != null }
                    .maxByOrNull { it.dateTime }

            // Check when it'll rain
            val forecastWithRain: HourlyRainForecast = ruleState.rainForecast
            //          - check time to rain
            //            if below next alert time, sent alert


            val alertsToSend: List<Map.Entry<String, AlertConfig>> =
                rule.alerts.getDeadlineAlertsToSend(latestSentAlert, ruleState)

            // Next alert is:
            //  - An alert for which the timeToDeadline has been passed (time until rain < timeToDeadline)
            //  - An alert should not have been sent before
            //  - The most strict alert should be chosen (if there are two alerts very close to one another, this means one could be skipped)
            val alertToSend = alertsToSend
                .firstOrNull { e ->
                    Duration.between(Instant.now(), forecastWithRain.dateTime) < e.value.timeToDeadline!!
                }

            if (alertToSend != null) {
                log.info("Reached timeLimit for alert ${alertToSend.key} at ${alertToSend.value.timeToDeadline} before rain")
                val properties = getAlertProperties(ruleState, rule, forecastWithRain)
                alertSenderService.send(alertToSend.value, properties)

                return SentAlert(
                    openedContactSensors = ruleState.openedContactSensors,
                    alertId = alertToSend.toAlertId(rule),
                    dateTime = Instant.now()
                )
            }
        }

        return null
    }

    // Next alert is:
    //  - An alert for which the timeToDeadline has been passed (expectedTimeToRain < timeToDeadline)
    //  - An alert should not have been sent before
    //  - The most strict alert should be chosen (if there are two alerts very close to one another, this means one could be skipped)
    /**
     * Alerts with deadline left to send are:
     *  - alerts that have not been sent yet.
     *  - alerts that have a tighter (i.e. shorten, smaller duration) deadline than the latest alert sent
     *
     *  @return a sorted List of the map entries of this map of alerts
     */
    private fun Map<String, AlertConfig>.getDeadlineAlertsToSend(
        latestSentAlert: SentAlert?,
        ruleState: RuleState
    ): List<Map.Entry<String, AlertConfig>> = this.entries
        .filter { e ->
            val alertId = e.toAlertId(ruleState.ruleId)
            val timeToDeadline = e.value.timeToDeadline
            timeToDeadline != null && timeToDeadline < (latestSentAlert?.alertId?.let { alerts[it] }?.timeToDeadline
                ?: Duration.ofDays(1))
                    && !ruleState.sentAlerts.map { a -> a.alertId }.contains(alertId)
        }

        .sortedBy { e -> e.value.timeToDeadline }
//        .map { it.key }

//    private suspend fun createTriggerForNextExpectedAlert(
//        alertsToSend: List<Map.Entry<String, Alert>>,
//        lastSentEvent: Map.Entry<String, Alert>?,
//        expectedTimeToRain: Duration
//    ) {
//        val lastTimeToDeadline = lastSentEvent?.value?.timeToDeadline ?: Duration.ofDays(2)
//
//        val firstExpectedAlert = alertsToSend
//            .filter { it.value.timeToDeadline != null }
//            .sortedByDescending { it.value.timeToDeadline }
//            .firstOrNull { e -> e.value.timeToDeadline!! < lastTimeToDeadline }
//
//        firstExpectedAlert?.let {
//            it.value.timeToDeadline?.let { nextAlertDeadline ->
//                val timeToNextAlert = expectedTimeToRain - nextAlertDeadline
//
//                timedEventsEventBus.publish(
//                    TimedUpdateRequest(
//                        Instant.now().plusSeconds(timeToNextAlert.toSeconds()),
//                        "Expected next time for alert ${it.key}"
//                    )
//                )
//            }
//        }
//    }

    private fun RainForecast.getFirstHourlyForecastWithRain(
        minimalVolume: Double = 0.1,
        minimalProbability: Double = 0.2
    ): HourlyRainForecast? =
        if (currentForecast.containsRainForecast(minimalVolume, minimalProbability)) {
            currentForecast
        } else {
            hourlyForecast.firstOrNull { it.containsRainForecast(minimalVolume, minimalProbability) }

        }

    private suspend fun trySendContactSensorChangeAlert(
        ruleState: RuleState,
        rule: Rule
    ): SentAlert? {
        if (ruleState.openedContactSensors != ruleState.sentAlerts.maxByOrNull { it.dateTime }?.openedContactSensors) {
            // send message
            val alert = alerts.entries.find {
                it.value.alertId.value.contains("every-contact-change") && it.value.alertId.value.contains(rule.id.value)
            }?.value

            alert?.let {
                val properties = getAlertProperties(ruleState, rule)
                alertSenderService.send(it, properties)


                return SentAlert(
                    openedContactSensors = ruleState.openedContactSensors,
                    alertId = it.alertId, // TODO make me pretty
                    dateTime = Instant.now()
                )

            }
        }
        return null
    }

    private fun getAlertProperties(
        ruleState: RuleState,
        rule: Rule,
        hourlyForecast: HourlyRainForecast? = null
    ) = mapOf(
        "openContactSensors" to
                ruleState.openedContactSensors.mapNotNull(String::toDeviceName).toString(),
        "allContactSensors" to
                rule.deviceIds.subtract(ruleState.openedContactSensors)
                    .mapNotNull(String::toDeviceName).toString(),
        "timeToRain" to if (hourlyForecast != null) "${
            Duration.between(Instant.now(), hourlyForecast.dateTime).toMinutes()
        }m" else "",
        "precipitationProb" to if (hourlyForecast != null) "${hourlyForecast.precipitationChance * 100}%" else "",
        "precipitationVolume" to if (hourlyForecast != null) "${hourlyForecast.precipitationVolume}mm" else ""
    )
}

