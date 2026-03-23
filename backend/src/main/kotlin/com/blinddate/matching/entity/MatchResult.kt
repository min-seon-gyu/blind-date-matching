package com.blinddate.matching.entity

import com.blinddate.common.entity.BaseEntity
import com.blinddate.event.entity.Event
import com.blinddate.participant.entity.Participant
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "match_result")
class MatchResult(
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "event_id", nullable = false) val event: Event,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "member1_id", nullable = false) val member1: Participant,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "member2_id", nullable = false) val member2: Participant,
    @Column(nullable = false) var notified: Boolean = false,
    var notifiedAt: LocalDateTime? = null
) : BaseEntity()
