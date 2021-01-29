package flock.community.office.monitoring.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class OfficeBackendApplication

fun main(args: Array<String>) {
    runApplication<OfficeBackendApplication>(*args)
}
