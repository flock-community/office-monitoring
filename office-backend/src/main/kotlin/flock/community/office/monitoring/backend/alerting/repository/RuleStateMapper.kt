package flock.community.office.monitoring.backend.alerting.repository

import flock.community.office.monitoring.backend.alerting.domain.AlertId
import flock.community.office.monitoring.backend.alerting.domain.RuleId
import flock.community.office.monitoring.backend.alerting.domain.AlertState
import flock.community.office.monitoring.backend.alerting.domain.AlertStateId
import flock.community.office.monitoring.backend.alerting.domain.SentAlert
import org.springframework.stereotype.Service

@Service
class RuleStateMapper {

    fun internalize(e: RuleStateEntity) = AlertState(
        id = AlertStateId(e.id),
        ruleId = RuleId(e.ruleId),
        active = e.active,
        lastStateChange = e.lastStateChange,
        sentAlerts = e.sentAlerts.internalize()
    )

    fun externalize(i: AlertState) = RuleStateEntity(
        id = i.id.value,
        ruleId = i.ruleId.value,
        active = i.active,
        lastStateChange = i.lastStateChange,
        sentAlerts = i.sentAlerts.externalize()

    )

    private fun List<SentAlertDto>.internalize(): List<SentAlert> = this.map {
        SentAlert(
            alertId = AlertId(it.alertId),
            dateTime = it.dateTime,
        )
    }

    private fun List<SentAlert>.externalize(): List<SentAlertDto> = this.map {
        SentAlertDto(
            alertId = it.alertId.value,
            dateTime = it.dateTime
        )
    }
}
