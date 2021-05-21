package flock.community.office.monitoring.backend.controller

import flock.community.office.monitoring.backend.domain.gcp.CommandPublisher
import flock.community.office.monitoring.utils.logging.loggerFor
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/switch/{deviceId}"])
internal class SwitchController(private val commandPublisher: CommandPublisher) {

    private val logger = loggerFor<SwitchController>()

    @PostMapping("/on")
    internal fun on(@PathVariable deviceId: String) {
        logger.info("Switch on")
        commandPublisher.on(deviceId)
    }

    @PostMapping("/off")
    internal fun off(@PathVariable deviceId: String) {
        logger.info("Switch off")
        commandPublisher.off(deviceId)
    }
}