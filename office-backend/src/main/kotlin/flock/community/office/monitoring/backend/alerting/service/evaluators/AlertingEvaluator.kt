package flock.community.office.monitoring.backend.alerting.service.evaluators

import flock.community.office.monitoring.backend.alerting.domain.Rule
import kotlinx.coroutines.flow.Flow

interface AlertingEvaluator<T, U> {
    fun subscribeToUpdates(rule: Rule): Flow<T>
    fun handleUpdate(update: T, previousStateValue: U): U
}
