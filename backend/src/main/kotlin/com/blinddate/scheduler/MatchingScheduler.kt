package com.blinddate.scheduler

import com.blinddate.application.entity.ApplicationStatus
import com.blinddate.application.repository.ApplicationRepository
import com.blinddate.commission.service.CommissionService
import com.blinddate.event.entity.EventStatus
import com.blinddate.event.repository.EventRepository
import com.blinddate.matching.repository.MatchResultRepository
import com.blinddate.matching.service.MatchingService
import com.blinddate.notification.entity.NotificationType
import com.blinddate.notification.entity.RecipientType
import com.blinddate.notification.service.NotificationService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class MatchingScheduler(
    private val eventRepository: EventRepository,
    private val matchingService: MatchingService,
    private val matchResultRepository: MatchResultRepository,
    private val applicationRepository: ApplicationRepository,
    private val notificationService: NotificationService,
    private val commissionService: CommissionService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedRate = 60000)
    @Transactional
    fun processMatchingAndNotify() {
        val now = LocalDateTime.now()
        val closedEvents = eventRepository.findByStatus(EventStatus.CLOSED)

        closedEvents
            .filter { it.matchNotificationTime != null && it.matchNotificationTime!!.isBefore(now) }
            .forEach { event ->
                try {
                    log.info("[MatchingScheduler] Processing matching for event=${event.id}")
                    matchingService.processMatching(event.id)

                    // Notify matched participants
                    val unnotifiedMatches = matchResultRepository.findByEventIdAndNotifiedFalse(event.id)
                    unnotifiedMatches.forEach { matchResult ->
                        notificationService.send(
                            RecipientType.PARTICIPANT, matchResult.member1.id,
                            NotificationType.MATCH_RESULT, "매칭 결과 안내",
                            "${event.title} 이벤트의 매칭 결과가 나왔습니다!"
                        )
                        notificationService.send(
                            RecipientType.PARTICIPANT, matchResult.member2.id,
                            NotificationType.MATCH_RESULT, "매칭 결과 안내",
                            "${event.title} 이벤트의 매칭 결과가 나왔습니다!"
                        )
                        matchResult.notified = true
                        matchResult.notifiedAt = LocalDateTime.now()
                    }

                    // Notify unmatched participants
                    val matchedIds = unnotifiedMatches.flatMap { listOf(it.member1.id, it.member2.id) }.toSet()
                    val allApproved = applicationRepository.findByEventIdAndStatus(event.id, ApplicationStatus.APPROVED)
                    allApproved.filter { it.participant.id !in matchedIds }.forEach { app ->
                        notificationService.send(
                            RecipientType.PARTICIPANT, app.participant.id,
                            NotificationType.MATCH_RESULT, "매칭 결과 안내",
                            "아쉽지만 이번에는 매칭되지 않았습니다."
                        )
                    }

                    // Notify organizer
                    val matchCount = matchResultRepository.findByEventId(event.id).size
                    val participantCount = allApproved.size
                    notificationService.send(
                        RecipientType.ORGANIZER, event.organizer.id,
                        NotificationType.EVENT_COMPLETED, "이벤트 완료",
                        "${event.title} 이벤트 완료! 참가자 ${participantCount}명, 매칭 ${matchCount}쌍"
                    )

                    // Complete event + create commission
                    event.status = EventStatus.COMPLETED
                    try { commissionService.createForEvent(event.id) } catch (e: Exception) {
                        log.warn("[MatchingScheduler] Commission creation failed for event=${event.id}: ${e.message}")
                    }

                } catch (e: Exception) {
                    log.warn("[MatchingScheduler] Error processing event=${event.id}: ${e.message}")
                }
            }
    }
}
