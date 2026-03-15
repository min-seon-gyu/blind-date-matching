package com.blinddate.scheduler

import com.blinddate.event.entity.EventStatus
import com.blinddate.event.repository.BlindDateEventRepository
import com.blinddate.matching.repository.MatchResultRepository
import com.blinddate.matching.service.MatchingService
import com.blinddate.notification.entity.NotificationType
import com.blinddate.notification.service.NotificationService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class MatchingScheduler(
    private val eventRepository: BlindDateEventRepository,
    private val matchingService: MatchingService,
    private val matchResultRepository: MatchResultRepository,
    private val notificationService: NotificationService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Every minute: process matching for events whose choice deadline has passed.
     */
    @Scheduled(fixedRate = 60000)
    fun processMatchingForClosedEvents() {
        val now = LocalDateTime.now()

        // Find OPEN or CLOSED events where choiceDeadline has passed
        val eligibleStatuses = listOf(EventStatus.OPEN, EventStatus.CLOSED)
        val events = eligibleStatuses.flatMap { status ->
            eventRepository.findByStatus(status)
        }.filter { event ->
            event.choiceDeadline != null && event.choiceDeadline!!.isBefore(now)
        }

        events.forEach { event ->
            try {
                log.info("[MatchingScheduler] Processing matching for event=${event.id}")
                matchingService.processMatching(event.id)
            } catch (e: Exception) {
                log.warn("[MatchingScheduler] Error processing matching for event=${event.id}: ${e.message}")
            }
        }

        // Send notifications for unnotified matches when matchNotificationTime has passed
        events.forEach { event ->
            if (event.matchNotificationTime != null && event.matchNotificationTime!!.isBefore(now)) {
                val unnotifiedMatches = matchResultRepository.findByEventIdAndNotifiedFalse(event.id)
                unnotifiedMatches.forEach { matchResult ->
                    try {
                        notificationService.send(
                            memberId = matchResult.member1.id,
                            type = NotificationType.MATCH_RESULT,
                            title = "매칭 결과 안내",
                            message = "${event.title} 이벤트의 매칭 결과가 나왔습니다!"
                        )
                        notificationService.send(
                            memberId = matchResult.member2.id,
                            type = NotificationType.MATCH_RESULT,
                            title = "매칭 결과 안내",
                            message = "${event.title} 이벤트의 매칭 결과가 나왔습니다!"
                        )
                        matchResult.notified = true
                        matchResult.notifiedAt = LocalDateTime.now()
                    } catch (e: Exception) {
                        log.warn("[MatchingScheduler] Error notifying match=${matchResult.id}: ${e.message}")
                    }
                }
            }
        }
    }
}
