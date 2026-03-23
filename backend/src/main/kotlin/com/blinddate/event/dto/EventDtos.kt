package com.blinddate.event.dto

import com.blinddate.event.entity.EventStatus
import com.blinddate.event.entity.MatchingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class EventResponse(
    val id: Long, val barId: Long, val title: String, val date: LocalDate, val time: LocalTime,
    val maleCapacity: Int, val femaleCapacity: Int, val currentMaleCount: Int, val currentFemaleCount: Int,
    val price: Int, val status: EventStatus, val description: String,
    val choiceDeadline: LocalDateTime?, val matchNotificationTime: LocalDateTime?,
    val minAge: Int?, val maxAge: Int?, val maxChoices: Int, val matchingMode: MatchingMode
)

data class EventCreateRequest(
    val title: String, val date: LocalDate, val time: LocalTime,
    val maleCapacity: Int, val femaleCapacity: Int, val price: Int,
    val description: String = "", val choiceDeadline: LocalDateTime? = null,
    val matchNotificationTime: LocalDateTime? = null, val minAge: Int? = null,
    val maxAge: Int? = null, val maxChoices: Int = 3, val matchingMode: MatchingMode = MatchingMode.BIDIRECTIONAL
)
