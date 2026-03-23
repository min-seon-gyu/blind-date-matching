package com.blinddate.scheduler

import com.blinddate.event.entity.EventStatus
import com.blinddate.event.repository.EventRepository
import com.blinddate.matching.service.ParticipantNumberService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class EventStatusScheduler(
    private val eventRepository: EventRepository,
    private val participantNumberService: ParticipantNumberService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedRate = 60000)
    @Transactional
    fun closeEventsAtDeadline() {
        val now = LocalDateTime.now()
        val openEvents = eventRepository.findByStatus(EventStatus.OPEN)

        openEvents.filter { it.choiceDeadline != null && it.choiceDeadline!!.isBefore(now) }
            .forEach { event ->
                try {
                    log.info("[EventStatusScheduler] Closing event=${event.id}, assigning numbers")
                    event.status = EventStatus.CLOSED
                    participantNumberService.assignNumbers(event.id)
                } catch (e: Exception) {
                    log.warn("[EventStatusScheduler] Error closing event=${event.id}: ${e.message}")
                }
            }
    }
}
