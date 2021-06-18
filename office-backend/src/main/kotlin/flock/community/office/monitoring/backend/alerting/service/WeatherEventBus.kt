package flock.community.office.monitoring.backend.alerting.service

import flock.community.office.monitoring.backend.weather.domain.WeatherForecast
import flock.community.office.monitoring.backend.weather.service.WeatherService
import flock.community.office.monitoring.utils.logging.loggerFor
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.springframework.beans.factory.DisposableBean
import org.springframework.stereotype.Component
import java.time.Duration


@Component
class WeatherEventBus(
    private val weatherService: WeatherService
) : DisposableBean {

    private val scope = CoroutineScope(CoroutineName("WeatherEventBus"))
    private val _events: MutableSharedFlow<WeatherForecast> = MutableSharedFlow(replay = 1)
    private val interval = Duration.ofMinutes(15)
    private val log = loggerFor<WeatherEventBus>()

    init {
        pollWeatherUpdates()
    }

    fun subscribe(): Flow<WeatherForecast> {
        return _events.asSharedFlow()
    }

    private fun pollWeatherUpdates() {
        scope.launch {
            do {
                getWeatherForecast()
                delay(interval.toMillis())
            } while (true)
        }
    }

    private suspend fun getWeatherForecast() {
        try {
            val deviceState = weatherService.getForecast()
            _events.emit(deviceState)
        } catch (ex: Throwable) {
            log.error(
                "Could not fetch new weather forecast. Will ignore result, but current forecast will get outdated soon. ${ex.message}",
                ex
            )
        }
    }

    override fun destroy() {
        log.info("Shutting down '${this::class.simpleName}'")
        scope.cancel("Stopping application")
    }
}
