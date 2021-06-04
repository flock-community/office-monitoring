package flock.community.office.monitoring.backend.controller

import flock.community.office.monitoring.backend.UpdatesModel
import flock.community.office.monitoring.backend.domain.repository.entities.DeviceStateEntity
import flock.community.office.monitoring.utils.logging.loggerFor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/device-updates"])
internal class RestStreamController(private val updatesModel: UpdatesModel) {

    private val logger = loggerFor<RestStreamController>()

    @GetMapping
    internal fun start(): Flow<DeviceStateEntity> = flow {
        logger.info("Receiving")
        updatesModel.state.onStart { emit(UpdatesModel.nullValue) }.collect { emit(it) }
    }
}
