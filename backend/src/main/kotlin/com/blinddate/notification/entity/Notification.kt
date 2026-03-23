package com.blinddate.notification.entity

import com.blinddate.common.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "notification")
class Notification(
    @Enumerated(EnumType.STRING) @Column(nullable = false) val recipientType: RecipientType,
    @Column(nullable = false) val recipientId: Long,
    @Enumerated(EnumType.STRING) @Column(nullable = false) val type: NotificationType,
    @Column(nullable = false) val title: String,
    @Column(columnDefinition = "TEXT", nullable = false) val message: String,
    @Column(nullable = false) var isRead: Boolean = false
) : BaseEntity()
