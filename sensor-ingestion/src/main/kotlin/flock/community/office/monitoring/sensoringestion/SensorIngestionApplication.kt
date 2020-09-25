package flock.community.office.monitoring.sensoringestion

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.integration.config.EnableIntegration
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler


@SpringBootApplication
@EnableScheduling
@EnableIntegration
class SensorIngestionApplication

fun main(args: Array<String>) {
    runApplication<SensorIngestionApplication>(*args)
}

@Bean
fun taskScheduler(): TaskScheduler? {
    //org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
    return ThreadPoolTaskScheduler()
}