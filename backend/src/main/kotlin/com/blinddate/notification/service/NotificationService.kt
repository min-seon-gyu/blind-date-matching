package com.blinddate.notification.service

import com.blinddate.common.exception.ForbiddenException
import com.blinddate.common.exception.NotFoundException
import com.blinddate.member.repository.MemberRepository
import com.blinddate.notification.dto.NotificationResponse
import com.blinddate.notification.dto.UnreadCountResponse
import com.blinddate.notification.entity.Notification
import com.blinddate.notification.entity.NotificationType
import com.blinddate.notification.repository.NotificationRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val memberRepository: MemberRepository,
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val NOTIFICATION_QUEUE = "notification:queue"
    }

    @Transactional
    fun send(memberId: Long, type: NotificationType, title: String, message: String) {
        val member = memberRepository.findById(memberId)
            .orElseThrow { NotFoundException("회원을 찾을 수 없습니다") }

        val notification = notificationRepository.save(
            Notification(member = member, type = type, title = title, message = message)
        )

        // Push to Redis queue as JSON
        try {
            val payload = mapOf(
                "notificationId" to notification.id,
                "memberId" to memberId,
                "type" to type.name,
                "title" to title,
                "message" to message,
                "phoneNumber" to member.phoneNumber
            )
            redisTemplate.opsForList().leftPush(NOTIFICATION_QUEUE, objectMapper.writeValueAsString(payload))
        } catch (e: Exception) {
            log.warn("Failed to push notification to Redis queue: ${e.message}")
        }
    }

    fun getNotifications(memberId: Long): List<NotificationResponse> =
        notificationRepository.findByMemberIdOrderByCreatedAtDesc(memberId)
            .map { it.toResponse() }

    @Transactional
    fun markAsRead(notificationId: Long, memberId: Long) {
        val notification = notificationRepository.findById(notificationId)
            .orElseThrow { NotFoundException("알림을 찾을 수 없습니다") }

        if (notification.member.id != memberId) {
            throw ForbiddenException("본인의 알림만 읽음 처리할 수 있습니다")
        }

        notification.isRead = true
    }

    fun getUnreadCount(memberId: Long): UnreadCountResponse =
        UnreadCountResponse(notificationRepository.countByMemberIdAndIsReadFalse(memberId))

    private fun Notification.toResponse() = NotificationResponse(
        id = id,
        type = type,
        title = title,
        message = message,
        isRead = isRead,
        createdAt = createdAt
    )
}
