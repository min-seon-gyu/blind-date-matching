package com.blinddate.payment.entity

import com.blinddate.application.entity.Application
import com.blinddate.common.entity.BaseEntity
import com.blinddate.member.entity.Member
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "payment")
class Payment(
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    val application: Application,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Column(nullable = false)
    val amount: Int,

    @Column(nullable = false)
    var paymentKey: String,

    @Column(unique = true, nullable = false)
    val orderId: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PaymentStatus = PaymentStatus.PENDING,

    var paidAt: LocalDateTime? = null,

    var refundedAt: LocalDateTime? = null
) : BaseEntity()
