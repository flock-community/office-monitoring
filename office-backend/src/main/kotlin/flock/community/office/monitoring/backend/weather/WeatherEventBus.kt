package flock.community.office.monitoring.backend.weather

import flock.community.office.monitoring.backend.weather.domain.WeatherForecastDto
import flock.community.office.monitoring.backend.weather.service.WeatherClient
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
    private val weatherClient: WeatherClient
) : DisposableBean {

    private val scope = CoroutineScope(CoroutineName("WeatherEventBus"))
    private val _events: MutableSharedFlow<WeatherForecastDto> = MutableSharedFlow(replay = 1)
    private val interval = Duration.ofMinutes(15)
    private val log = loggerFor<WeatherEventBus>()

    init {
        pollWeatherUpdates() // disable me for local testing
    }

    fun subscribe(): Flow<WeatherForecastDto> {
        return _events.asSharedFlow()
    }

    suspend fun publish(forecast: WeatherForecastDto){
        _events.emit(forecast)
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
            val forecast = weatherClient.getForecast()
            publish(forecast)
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
