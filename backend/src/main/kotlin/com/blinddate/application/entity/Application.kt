package com.blinddate.application.entity

import com.blinddate.common.entity.BaseEntity
import com.blinddate.event.entity.BlindDateEvent
import com.blinddate.member.entity.Member
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "application",
    uniqueConstraints = [UniqueConstraint(columnNames = ["member_id", "event_id"])]
)
class Application(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    val event: BlindDateEvent,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ApplicationStatus = ApplicationStatus.PAYMENT_WAITING,

    @Column(nullable = false)
    var appliedAt: LocalDateTime = LocalDateTime.now(),

    var reviewedAt: LocalDateTime? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    var reviewedBy: Member? = null,

    @Column(columnDefinition = "TEXT")
    var rejectReason: String? = null
) : BaseEntity()
