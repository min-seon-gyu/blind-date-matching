package com.blinddate.bar.entity

import com.blinddate.common.entity.BaseEntity
import com.blinddate.member.entity.Member
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalTime

@Entity
@Table(name = "bar_reservation")
class BarReservation(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bar_id", nullable = false)
    val bar: Bar,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Column(nullable = false)
    val date: LocalDate,

    @Column(nullable = false)
    val time: LocalTime,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: BarReservationStatus = BarReservationStatus.CONFIRMED
) : BaseEntity()
