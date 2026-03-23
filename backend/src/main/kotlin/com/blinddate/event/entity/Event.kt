package com.blinddate.event.entity

import com.blinddate.bar.entity.Bar
import com.blinddate.common.entity.BaseEntity
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity
@Table(name = "event")
class Event(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bar_id", nullable = false)
    val bar: Bar,

    @Column(nullable = false) var title: String,
    @Column(nullable = false) var date: LocalDate,
    @Column(nullable = false) var time: LocalTime,
    @Column(nullable = false) var price: Int,
    @Column(nullable = false) var maleCapacity: Int,
    @Column(nullable = false) var femaleCapacity: Int,
    @Column(nullable = false) var currentMaleCount: Int = 0,
    @Column(nullable = false) var currentFemaleCount: Int = 0,
    @Column(columnDefinition = "TEXT") var description: String = "",
    var choiceDeadline: LocalDateTime? = null,
    var matchNotificationTime: LocalDateTime? = null,
    var minAge: Int? = null,
    var maxAge: Int? = null,
    @Column(nullable = false) var maxChoices: Int = 3,
    @Enumerated(EnumType.STRING) @Column(nullable = false) var matchingMode: MatchingMode = MatchingMode.BIDIRECTIONAL,
    @Enumerated(EnumType.STRING) @Column(nullable = false) var status: EventStatus = EventStatus.OPEN,
    var deletedAt: LocalDateTime? = null
) : BaseEntity() {
    @Version
    var version: Long = 0

    fun isDeleted() = deletedAt != null
    fun hasAvailableMaleSlots() = currentMaleCount < maleCapacity
    fun hasAvailableFemaleSlots() = currentFemaleCount < femaleCapacity
}
