package com.blinddate.commission.entity

import com.blinddate.cafe.entity.Cafe
import com.blinddate.common.entity.BaseEntity
import com.blinddate.event.entity.Event
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "commission")
class Commission(
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "cafe_id", nullable = false) val cafe: Cafe,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "event_id", nullable = false) val event: Event,
    @Enumerated(EnumType.STRING) @Column(nullable = false) val targetType: CommissionTargetType,
    @Column(nullable = false) val targetId: Long,
    @Column(nullable = false) val participantCount: Int,
    @Column(nullable = false) val eventPrice: Int,
    @Column(nullable = false) val commissionRate: Int,
    @Column(nullable = false) val unitPrice: Int,
    @Column(nullable = false) val totalAmount: Int,
    @Enumerated(EnumType.STRING) @Column(nullable = false) var status: CommissionStatus = CommissionStatus.PENDING,
    var invoicedAt: LocalDateTime? = null,
    var paidAt: LocalDateTime? = null
) : BaseEntity()
