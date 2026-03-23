package com.blinddate.matching.entity

import com.blinddate.common.entity.BaseEntity
import com.blinddate.event.entity.Event
import com.blinddate.participant.entity.Gender
import com.blinddate.participant.entity.Participant
import jakarta.persistence.*

@Entity
@Table(name = "participant_number",
    uniqueConstraints = [UniqueConstraint(columnNames = ["event_id", "participant_id"])])
class ParticipantNumber(
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "event_id", nullable = false) val event: Event,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "participant_id", nullable = false) val participant: Participant,
    @Column(nullable = false) val number: Int,
    @Enumerated(EnumType.STRING) @Column(nullable = false) val gender: Gender
) : BaseEntity()
