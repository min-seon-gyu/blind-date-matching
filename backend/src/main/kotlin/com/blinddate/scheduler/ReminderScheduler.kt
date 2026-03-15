package com.blinddate.scheduler

import com.blinddate.application.entity.ApplicationStatus
import com.blinddate.application.repository.ApplicationRepository
import com.blinddate.event.entity.EventStatus
import com.blinddate.event.repository.BlindDateEventRepository
import com.blinddate.notification.entity.NotificationType
import com.blinddate.notification.service.NotificationService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class ReminderScheduler(
    private val eventRepository: BlindDateEventRepository,
    private val applicationRepository: ApplicationRepository,
    private val notificationService: NotificationService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Daily at 10am: send reminders to participants of events happening tomorrow (D-1).
     */
    @Scheduled(cron = "0 0 10 * * *")
    fun sendEventReminders() {
        val tomorrow = LocalDate.now().plusDays(1)

        val openEvents = eventRepository.findByStatus(EventStatus.OPEN)
        val tomorrowEvents = openEvents.filter { it.date == tomorrow }

        tomorrowEvents.forEach { event ->
            val approvedApplications = applicationRepository.findByEventIdAndStatus(
                event.id, ApplicationStatus.APPROVED
            )

            approvedApplications.forEach { application ->
                try {
                    notificationService.send(
                        memberId = application.member.id,
                        type = NotificationType.EVENT_REMINDER,
                        title = "이벤트 D-1 알림",
                        message = "내일 [${event.title}] 이벤트가 있습니다. ${event.time}에 준비해 주세요!"
                    )
                } catch (e: Exception) {
                    log.warn("[ReminderScheduler] Error sending reminder for member=${application.member.id}: ${e.message}")
                }
            }
            log.info("[ReminderScheduler] Sent ${approvedApplications.size} reminders for event=${event.id}")
        }
    }
}
