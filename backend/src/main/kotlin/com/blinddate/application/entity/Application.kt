package com.blinddate.application.entity

import com.blinddate.barowner.entity.BarOwner
import com.blinddate.common.entity.BaseEntity
import com.blinddate.event.entity.Event
import com.blinddate.participant.entity.Participant
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "application",
    uniqueConstraints = [UniqueConstraint(columnNames = ["participant_id", "event_id"])])
class Application(
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "participant_id", nullable = false)
    val participant: Participant,

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "event_id", nullable = false)
    val event: Event,

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    var status: ApplicationStatus = ApplicationStatus.PENDING,

    @Column(nullable = false) var appliedAt: LocalDateTime = LocalDateTime.now(),
    var reviewedAt: LocalDateTime? = null,

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "reviewed_by")
    var reviewedBy: BarOwner? = null,

    @Column(columnDefinition = "TEXT") var rejectReason: String? = null
) : BaseEntity()
