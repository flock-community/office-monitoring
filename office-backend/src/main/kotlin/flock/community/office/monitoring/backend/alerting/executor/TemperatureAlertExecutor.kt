package flock.community.office.monitoring.backend.alerting.executor

import flock.community.office.monitoring.backend.alerting.domain.AlertChannel
import flock.community.office.monitoring.backend.alerting.service.AlertSenderService
import flock.community.office.monitoring.backend.device.configuration.deviceIdToSensorIdMapping
import flock.community.office.monitoring.backend.device.domain.DeviceState
import flock.community.office.monitoring.backend.device.domain.TemperatureSensorStateBody
import flock.community.office.monitoring.backend.device.service.DeviceStateHistoryService
import flock.community.office.monitoring.utils.logging.loggerFor
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TemperatureAlertExecutor(
    val pastEvents: DeviceStateHistoryService,
    val alertService: AlertSenderService
) {
    val logger = loggerFor<TemperatureAlertExecutor>()

    val scope = CoroutineScope(CoroutineName("TemperatureAlertScope"))

    @Scheduled(cron = "* * 20 * * MON-FRI")
    fun checkTemperatureAsExpected() {
        scope.launch {
            logger.info("het is nu ${LocalDateTime.now()}"  )
            val temperatureEvent =
                pastEvents
                    .getLatest(deviceIdToSensorIdMapping["505896d1-1b7a-4a58-9dbc-b28c39ddecfa"].toString()) //TODO get id from config
                        as DeviceState<TemperatureSensorStateBody>
            logger.info("Received temperature event")
            if (temperatureTooHigh(temperatureEvent.state)) {
                alertService.send(
                    "It's still {{temperature}} degrees at the office, someone forgot to turn off the heating!",
                    AlertChannel.SIGNAL,
                    mapOf("temperature" to temperatureEvent.state.temperature.toString())
                )
            }
        }
    }

    fun temperatureTooHigh(temperatureStateBody: TemperatureSensorStateBody): Boolean {
        return temperatureStateBody.temperature > 10
    }
}