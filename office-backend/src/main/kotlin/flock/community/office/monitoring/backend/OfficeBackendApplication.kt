package flock.community.office.monitoring.backend

import flock.community.office.monitoring.backend.alerting.service.AlertingService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.scheduling.annotation.EnableScheduling


@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
class OfficeBackendApplication

fun main(args: Array<String>) {
    val applicationContext: ConfigurableApplicationContext = runApplication<OfficeBackendApplication>(*args)


    val getRidOfDuplicatesService = applicationContext.getBean(AlertingService::class.java)
    getRidOfDuplicatesService.start()
}

