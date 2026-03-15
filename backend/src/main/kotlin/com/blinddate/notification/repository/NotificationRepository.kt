package com.blinddate.notification.repository

import com.blinddate.notification.entity.Notification
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationRepository : JpaRepository<Notification, Long> {
    fun findByMemberIdOrderByCreatedAtDesc(memberId: Long): List<Notification>
    fun countByMemberIdAndIsReadFalse(memberId: Long): Long
}
