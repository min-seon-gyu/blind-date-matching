package com.blinddate.notification.repository

import com.blinddate.notification.entity.Notification
import com.blinddate.notification.entity.RecipientType
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationRepository : JpaRepository<Notification, Long> {
    fun findByRecipientTypeAndRecipientIdOrderByCreatedAtDesc(recipientType: RecipientType, recipientId: Long): List<Notification>
    fun countByRecipientTypeAndRecipientIdAndIsReadFalse(recipientType: RecipientType, recipientId: Long): Long
}
