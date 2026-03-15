package com.blinddate.matching.entity

import com.blinddate.common.entity.BaseEntity
import com.blinddate.event.entity.BlindDateEvent
import com.blinddate.member.entity.Member
import jakarta.persistence.*

@Entity
@Table(name = "choice")
class Choice(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    val event: BlindDateEvent,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chooser_id", nullable = false)
    val chooser: Member,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chosen_id", nullable = false)
    val chosen: Member
) : BaseEntity()
