package com.blinddate.matching.entity

import com.blinddate.common.entity.BaseEntity
import com.blinddate.event.entity.BlindDateEvent
import com.blinddate.member.entity.Member
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "match_result",
    uniqueConstraints = [UniqueConstraint(columnNames = ["event_id", "member1_id", "member2_id"])]
)
class MatchResult(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    val event: BlindDateEvent,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member1_id", nullable = false)
    val member1: Member,  // always MALE

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member2_id", nullable = false)
    val member2: Member,  // always FEMALE

    @Column(nullable = false)
    var notified: Boolean = false,

    var notifiedAt: LocalDateTime? = null
) : BaseEntity()
