package com.blinddate.partnership.entity

import com.blinddate.cafe.entity.Cafe
import com.blinddate.common.entity.BaseEntity
import com.blinddate.organizer.entity.Organizer
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "partnership")
class Partnership(
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "cafe_id", nullable = false) val cafe: Cafe,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "organizer_id", nullable = false) val organizer: Organizer,
    @Enumerated(EnumType.STRING) @Column(nullable = false) var status: PartnershipStatus = PartnershipStatus.PENDING,
    @Enumerated(EnumType.STRING) @Column(nullable = false) val requestedBy: PartnershipRequester,
    @Column(columnDefinition = "TEXT") var message: String? = null,
    var respondedAt: LocalDateTime? = null,
    var terminatedAt: LocalDateTime? = null
) : BaseEntity()
