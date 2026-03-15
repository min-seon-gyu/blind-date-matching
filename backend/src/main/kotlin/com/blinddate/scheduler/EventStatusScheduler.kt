package com.blinddate.scheduler

import com.blinddate.application.entity.ApplicationStatus
import com.blinddate.application.repository.ApplicationRepository
import com.blinddate.event.entity.EventStatus
import com.blinddate.event.repository.BlindDateEventRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Component
class EventStatusScheduler(
    private val eventRepository: BlindDateEventRepository,
    private val applicationRepository: ApplicationRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Daily at midnight: mark past events as COMPLETED and complete their approved applications.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    fun updateCompletedEvents() {
        val today = LocalDate.now()

        val openEvents = eventRepository.findByStatus(EventStatus.OPEN)
        val expiredEvents = openEvents.filter { it.date.isBefore(today) }

        expiredEvents.forEach { event ->
            try {
                log.info("[EventStatusScheduler] Completing event=${event.id} date=${event.date}")
                event.status = EventStatus.COMPLETED

                // Complete all approved applications for this event
                val approvedApplications = applicationRepository.findByEventIdAndStatus(
                    event.id, ApplicationStatus.APPROVED
                )
                approvedApplications.forEach { application ->
                    application.status = ApplicationStatus.COMPLETED
                }
            } catch (e: Exception) {
                log.warn("[EventStatusScheduler] Error completing event=${event.id}: ${e.message}")
            }
        }
    }
}
