package flock.community.office.monitoring.backend.alerting.service.evaluators

import flock.community.office.monitoring.backend.alerting.domain.ContactSensorUpdate
import flock.community.office.monitoring.backend.alerting.domain.Rule
import flock.community.office.monitoring.backend.device.DeviceStateEventBus
import flock.community.office.monitoring.backend.device.configuration.devicesMappingConfigurations
import flock.community.office.monitoring.backend.device.domain.ContactSensorStateBody
import flock.community.office.monitoring.backend.device.domain.DeviceState
import flock.community.office.monitoring.backend.device.domain.StateBody
import flock.community.office.monitoring.backend.device.service.DeviceStateHistoryService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import org.springframework.stereotype.Component
import java.util.stream.Collectors

@Component
class DeviceStateEvaluator(
    private val deviceStateEventBus: DeviceStateEventBus,
    private val historyService: DeviceStateHistoryService
) : AlertingEvaluator<ContactSensorUpdate, Set<String>> {

    override fun subscribeToUpdates(rule: Rule): Flow<ContactSensorUpdate> = flow {
        val sensorIds = rule.deviceIds.map { deviceId ->
            devicesMappingConfigurations.entries.first { it.value.deviceId == deviceId }.key
        }
        val latest: Flow<ContactSensorUpdate> = sensorIds
            .parallelStream()
            .map {
                historyService.getLatest(it)
            }
            .map { toContactSensorUpdate(it) }
            .collect(Collectors.toList())
            .filterNotNull()
            .asFlow()

        emitAll(latest)

        deviceStateEventBus.subscribe(null).filter { a -> sensorIds.contains(a.sensorId) }
            .mapNotNull { toContactSensorUpdate(it) }
            .collect { emit(it) }

    }

    fun toContactSensorUpdate(deviceState: DeviceState<StateBody>?) =
        if (deviceState != null && deviceState.state is ContactSensorStateBody) {
            ContactSensorUpdate(
                deviceId = deviceState.deviceId,
                date = deviceState.state.lastSeen,
                contact = deviceState.state.contact
            )
        } else {
            null
        }



    /**
     *
     * Updates the event based on contact state of a sensor
     *      -  Keeps track of open contact sensors
     */
    override suspend fun handleUpdate(update: ContactSensorUpdate, previousStateValue: Set<String>): Set<String> {

        val previousContactState =
            !previousStateValue.contains(update.deviceId) // no contact means present in list

        return if (update.contact != previousContactState) {
            when (update.contact) {
                true -> previousStateValue - update.deviceId
                false -> previousStateValue + update.deviceId
            }
        } else {
            previousStateValue
        }
    }

}
