package com.blinddate.event.entity

import com.blinddate.common.entity.BaseEntity
import com.blinddate.member.entity.Member
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity
@Table(name = "blind_date_event")
class BlindDateEvent(
    @Column(nullable = false) var title: String,
    @Column(nullable = false) var date: LocalDate,
    @Column(nullable = false) var time: LocalTime,
    @Column(nullable = false) var maleCapacity: Int,
    @Column(nullable = false) var femaleCapacity: Int,
    @Column(nullable = false) var currentMaleCount: Int = 0,
    @Column(nullable = false) var currentFemaleCount: Int = 0,
    @Column(nullable = false) var price: Int,
    @Enumerated(EnumType.STRING) var status: EventStatus = EventStatus.OPEN,
    @Column(columnDefinition = "TEXT") var description: String = "",
    var choiceDeadline: LocalDateTime? = null,
    var matchNotificationTime: LocalDateTime? = null,
    var minAge: Int? = null,
    var maxAge: Int? = null,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "created_by") val createdBy: Member,
    var deletedAt: LocalDateTime? = null
) : BaseEntity() {
    fun isDeleted() = deletedAt != null
    fun hasAvailableMaleSlots() = currentMaleCount < maleCapacity
    fun hasAvailableFemaleSlots() = currentFemaleCount < femaleCapacity
}
