package com.blinddate.bar.entity

import com.blinddate.common.entity.BaseEntity
import com.blinddate.member.entity.Gender
import com.blinddate.member.entity.Member
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "bar_visit_log")
class BarVisitLog(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bar_id", nullable = false)
    val bar: Bar,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    val member: Member? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    val reservation: BarReservation? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val gender: Gender,

    @Column(nullable = false)
    val checkInAt: LocalDateTime = LocalDateTime.now(),

    @Column
    var checkOutAt: LocalDateTime? = null
) : BaseEntity()
