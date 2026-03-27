package com.blinddate.organizer.dto

import com.blinddate.event.entity.MatchingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class CreateEventRequest(
    val cafeId: Long,
    val title: String,
    val date: LocalDate,
    val time: LocalTime,
    val price: Int,
    val maleCapacity: Int,
    val femaleCapacity: Int,
    val description: String = "",
    val choiceDeadline: LocalDateTime? = null,
    val matchNotificationTime: LocalDateTime? = null,
    val minAge: Int? = null,
    val maxAge: Int? = null,
    val maxChoices: Int = 3,
    val matchingMode: MatchingMode = MatchingMode.BIDIRECTIONAL
)

data class UpdateEventRequest(
    val title: String,
    val date: LocalDate,
    val time: LocalTime,
    val price: Int,
    val maleCapacity: Int,
    val femaleCapacity: Int,
    val description: String = "",
    val choiceDeadline: LocalDateTime? = null,
    val matchNotificationTime: LocalDateTime? = null,
    val minAge: Int? = null,
    val maxAge: Int? = null,
    val maxChoices: Int = 3,
    val matchingMode: MatchingMode = MatchingMode.BIDIRECTIONAL
)
