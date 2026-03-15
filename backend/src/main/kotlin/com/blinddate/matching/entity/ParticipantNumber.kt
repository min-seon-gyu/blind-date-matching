package com.blinddate.matching.entity

import com.blinddate.common.entity.BaseEntity
import com.blinddate.event.entity.BlindDateEvent
import com.blinddate.member.entity.Gender
import com.blinddate.member.entity.Member
import jakarta.persistence.*

@Entity
@Table(
    name = "participant_number",
    uniqueConstraints = [UniqueConstraint(columnNames = ["event_id", "gender", "number"])]
)
class ParticipantNumber(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    val event: BlindDateEvent,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Column(nullable = false)
    val number: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val gender: Gender
) : BaseEntity()
