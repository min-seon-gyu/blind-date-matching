package com.blinddate.notification.entity

import com.blinddate.common.entity.BaseEntity
import com.blinddate.member.entity.Member
import jakarta.persistence.*

@Entity
@Table(name = "notification")
class Notification(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: NotificationType,

    @Column(nullable = false)
    val title: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val message: String,

    @Column(nullable = false)
    var isRead: Boolean = false
) : BaseEntity()
