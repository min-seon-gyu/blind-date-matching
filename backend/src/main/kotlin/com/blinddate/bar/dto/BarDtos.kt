package com.blinddate.bar.dto

import com.blinddate.bar.entity.BarReservationStatus
import com.blinddate.member.entity.Gender
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class BarStatusResponse(
    val id: Long,
    val name: String,
    val address: String,
    val totalSeats: Int,
    val currentMaleCount: Long,
    val currentFemaleCount: Long,
    val remainingSeats: Long,
    val isOpen: Boolean,
    val openTime: LocalTime,
    val closeTime: LocalTime
)

data class BarStatusDto(
    val maleCount: Long,
    val femaleCount: Long
)

data class BarReserveRequest(
    val date: LocalDate,
    val time: LocalTime
)

data class BarReservationResponse(
    val id: Long,
    val barId: Long,
    val date: LocalDate,
    val time: LocalTime,
    val status: BarReservationStatus,
    val createdAt: LocalDateTime
)

data class CheckInRequest(
    val gender: Gender,
    val memberId: Long? = null
)

data class VisitorResponse(
    val visitLogId: Long,
    val gender: Gender,
    val memberId: Long?,
    val memberName: String?,
    val checkInAt: LocalDateTime
)
