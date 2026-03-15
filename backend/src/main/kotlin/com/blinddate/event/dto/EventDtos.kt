package com.blinddate.event.dto

import com.blinddate.event.entity.EventStatus
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class EventCreateRequest(
    @field:NotBlank val title: String,
    val date: LocalDate, val time: LocalTime,
    @field:Min(1) val maleCapacity: Int, @field:Min(1) val femaleCapacity: Int,
    @field:Min(0) val price: Int, val description: String = "",
    val choiceDeadline: LocalDateTime? = null, val matchNotificationTime: LocalDateTime? = null,
    val minAge: Int? = null, val maxAge: Int? = null
)

data class EventResponse(
    val id: Long, val title: String, val date: LocalDate, val time: LocalTime,
    val maleCapacity: Int, val femaleCapacity: Int,
    val currentMaleCount: Int, val currentFemaleCount: Int,
    val price: Int, val status: EventStatus, val description: String,
    val choiceDeadline: LocalDateTime?, val matchNotificationTime: LocalDateTime?,
    val minAge: Int?, val maxAge: Int?
)
