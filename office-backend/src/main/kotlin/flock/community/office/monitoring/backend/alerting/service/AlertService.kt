package flock.community.office.monitoring.backend.alerting.service

import flock.community.office.monitoring.backend.alerting.domain.Alert
import org.springframework.stereotype.Service

@Service
class AlertService {
    fun send( alert: Alert): Boolean{
        println("Sending alert: ${alert.message} (channel ${alert.channel})");
        return true
    }
}
