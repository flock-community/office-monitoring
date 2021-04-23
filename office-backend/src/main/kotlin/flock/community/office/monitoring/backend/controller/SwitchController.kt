package flock.community.office.monitoring.backend.controller

import flock.community.office.monitoring.backend.domain.gcp.CommandPublisher
import flock.community.office.monitoring.utils.logging.loggerFor
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/switch"])
internal class SwitchController(private val commandPublisher: CommandPublisher) {

    private val logger = loggerFor<SwitchController>()

    @GetMapping
    internal fun on() {
        logger.info("Switch on")
        commandPublisher.on("deviceId")
    }
}