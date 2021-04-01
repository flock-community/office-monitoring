package flock.community.office.monitoring.backend

import flock.community.office.monitoring.backend.domain.service.GetRidOfDuplicatesService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

import org.springframework.context.ConfigurableApplicationContext


@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
class OfficeBackendApplication

fun main(args: Array<String>) {
    val applicationContext: ConfigurableApplicationContext = runApplication<OfficeBackendApplication>(*args)


    val getRidOfDuplicatesService = applicationContext.getBean(GetRidOfDuplicatesService::class.java)
    getRidOfDuplicatesService.deduplicate()
}

