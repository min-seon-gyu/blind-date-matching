package com.blinddate.matching.entity

import com.blinddate.common.entity.BaseEntity
import com.blinddate.event.entity.Event
import com.blinddate.participant.entity.Participant
import jakarta.persistence.*

@Entity
@Table(name = "choice")
class Choice(
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "event_id", nullable = false) val event: Event,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "chooser_id", nullable = false) val chooser: Participant,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "chosen_id", nullable = false) val chosen: Participant
) : BaseEntity()
