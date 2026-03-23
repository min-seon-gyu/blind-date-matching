package com.blinddate.notification.dto

import com.blinddate.notification.entity.NotificationType
import java.time.LocalDateTime

data class NotificationResponse(
    val id: Long,
    val type: NotificationType,
    val title: String,
    val message: String,
    val isRead: Boolean,
    val createdAt: LocalDateTime
)

data class UnreadCountResponse(val count: Long)
