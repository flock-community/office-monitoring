package flock.community.office.monitoring.backend.alerting.service.evaluators

import flock.community.office.monitoring.backend.alerting.domain.HourlyRainForecast
import flock.community.office.monitoring.backend.alerting.domain.RainForecast
import flock.community.office.monitoring.backend.alerting.domain.Rule
import flock.community.office.monitoring.backend.alerting.domain.containsRainForecast
import flock.community.office.monitoring.backend.alerting.executor.toRainUpdate
import flock.community.office.monitoring.backend.weather.WeatherEventBus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Component

@Component
class RainForecastEvaluator(
    private val weatherEventBus: WeatherEventBus
): AlertingEvaluator<RainForecast, HourlyRainForecast?> {


    override fun subscribeToUpdates(rule: Rule): Flow<RainForecast> = weatherEventBus.subscribe()
        .map { it.toRainUpdate() }


    override suspend fun handleUpdate(update: RainForecast, previousStateValue: HourlyRainForecast?): HourlyRainForecast? {
        val hourlyForecastUpperBoundVolume = 0.2
        val hourlyForecastUpperBoundProbability = 0.7
        val firstHourlyForecastWithRain = update.getFirstHourlyForecastWithRain(hourlyForecastUpperBoundVolume, hourlyForecastUpperBoundProbability)

        // Check whether the previous rain forecast is still applicable
        if (previousStateValue != null) {
            val newForecastAtLastPrediction =
                update.hourlyForecast.find { it.dateTime == previousStateValue.dateTime }

            // check whether there's still rain predicted at the previously predicted time (Tn-1):
            //  - will it still rain at Tn-1?
            //  - is the previously predicted time (Tn-1) earlier than the currently predicted time (Tn)
            if (newForecastAtLastPrediction.containsRainForecast(0.1, 0.5)
                && (firstHourlyForecastWithRain == null || previousStateValue.dateTime < firstHourlyForecastWithRain.dateTime)
            ) {
                // previous prediction is leading
                return newForecastAtLastPrediction
            }
        }

        return firstHourlyForecastWithRain
    }


    private fun RainForecast.getFirstHourlyForecastWithRain(
        minimalVolume: Double = 0.1,
        minimalProbability: Double = 0.2
    ): HourlyRainForecast? =
        if (currentForecast.containsRainForecast(minimalVolume, minimalProbability)) {
            currentForecast
        } else {
            hourlyForecast.firstOrNull { it.containsRainForecast(minimalVolume, minimalProbability) }

        }


}
