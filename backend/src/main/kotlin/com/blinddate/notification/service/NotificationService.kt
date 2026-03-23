package com.blinddate.notification.service

import com.blinddate.common.exception.ForbiddenException
import com.blinddate.common.exception.NotFoundException
import com.blinddate.notification.dto.NotificationResponse
import com.blinddate.notification.dto.UnreadCountResponse
import com.blinddate.notification.entity.Notification
import com.blinddate.notification.entity.NotificationType
import com.blinddate.notification.entity.RecipientType
import com.blinddate.notification.repository.NotificationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class NotificationService(
    private val notificationRepository: NotificationRepository
) {
    @Transactional
    fun send(recipientType: RecipientType, recipientId: Long, type: NotificationType, title: String, message: String): NotificationResponse {
        val notification = notificationRepository.save(
            Notification(recipientType = recipientType, recipientId = recipientId, type = type, title = title, message = message)
        )
        return notification.toResponse()
    }

    fun getNotifications(recipientType: RecipientType, recipientId: Long): List<NotificationResponse> =
        notificationRepository.findByRecipientTypeAndRecipientIdOrderByCreatedAtDesc(recipientType, recipientId)
            .map { it.toResponse() }

    @Transactional
    fun markAsRead(notificationId: Long, recipientType: RecipientType, recipientId: Long) {
        val notification = notificationRepository.findById(notificationId)
            .orElseThrow { NotFoundException("알림을 찾을 수 없습니다") }
        if (notification.recipientType != recipientType || notification.recipientId != recipientId) {
            throw ForbiddenException("본인의 알림만 읽음 처리할 수 있습니다")
        }
        notification.isRead = true
    }

    fun getUnreadCount(recipientType: RecipientType, recipientId: Long): UnreadCountResponse =
        UnreadCountResponse(notificationRepository.countByRecipientTypeAndRecipientIdAndIsReadFalse(recipientType, recipientId))

    private fun Notification.toResponse() = NotificationResponse(
        id = id, type = type, title = title, message = message, isRead = isRead, createdAt = createdAt
    )
}
