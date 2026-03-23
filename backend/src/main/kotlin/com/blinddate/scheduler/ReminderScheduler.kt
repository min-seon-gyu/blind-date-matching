package com.blinddate.scheduler

import com.blinddate.application.entity.ApplicationStatus
import com.blinddate.application.repository.ApplicationRepository
import com.blinddate.event.entity.EventStatus
import com.blinddate.event.repository.EventRepository
import com.blinddate.notification.entity.NotificationType
import com.blinddate.notification.entity.RecipientType
import com.blinddate.notification.service.NotificationService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class ReminderScheduler(
    private val eventRepository: EventRepository,
    private val applicationRepository: ApplicationRepository,
    private val notificationService: NotificationService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "0 0 10 * * *")
    fun sendReminders() {
        val tomorrow = LocalDate.now().plusDays(1)
        val openEvents = eventRepository.findByStatus(EventStatus.OPEN)

        openEvents.filter { it.date == tomorrow }.forEach { event ->
            val approved = applicationRepository.findByEventIdAndStatus(event.id, ApplicationStatus.APPROVED)
            approved.forEach { app ->
                try {
                    notificationService.send(
                        RecipientType.PARTICIPANT, app.participant.id,
                        NotificationType.EVENT_REMINDER, "내일 이벤트가 있습니다!",
                        "${event.title} 이벤트가 내일 ${event.time}에 시작됩니다."
                    )
                } catch (e: Exception) {
                    log.warn("[ReminderScheduler] Error sending reminder: ${e.message}")
                }
            }
        }
    }
}
