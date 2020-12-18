package flock.community.office.monitoring.backend.data.service

import flock.community.office.monitoring.backend.data.repository.EventRepository
import org.springframework.stereotype.Service

@Service
class EventSaveService(
    val eventRepository: EventRepository
) {


}
