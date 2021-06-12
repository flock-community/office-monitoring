package flock.community.office.monitoring.backend.alerting.service

import flock.community.office.monitoring.backend.weather.domain.WeatherPrediction
import flock.community.office.monitoring.backend.weather.service.WeatherService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import java.time.Duration


@Component
class WeatherEventBus(
    private val weatherService: WeatherService

) {
    private val _events: MutableSharedFlow<WeatherPrediction> = MutableSharedFlow(replay = 1)

    private val interval = Duration.ofMinutes(15);

    private fun publish(deviceState: WeatherPrediction): Boolean {
        return _events.tryEmit(deviceState)
    }

    private fun requestWeatherUpdates() {
        if (_events.subscriptionCount.value > 0) return

        GlobalScope.launch {
            do {
                weatherService.getPrediction()?.let { publish(it) }
                delay(interval.toMillis())
            } while (_events.subscriptionCount.value > 0)
        }
    }

    fun subscribe(): Flow<WeatherPrediction> {
        requestWeatherUpdates()
        return _events.asSharedFlow()
    }


}
