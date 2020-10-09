package flock.community.office.monitoring.backend.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler

@Configuration
class Config {

    @Bean
    fun taskScheduler(): TaskScheduler = ThreadPoolTaskScheduler()

}
