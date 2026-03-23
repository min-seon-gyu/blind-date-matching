package com.blinddate.barowner.dto

import com.blinddate.event.entity.EventStatus

data class BarUpdateRequest(
    val name: String,
    val address: String,
    val description: String = "",
    val logoUrl: String = "",
    val coverImageUrl: String = ""
)

data class EventStatsResponse(
    val eventId: Long,
    val title: String,
    val participantCount: Int,
    val maleCount: Int,
    val femaleCount: Int,
    val matchCount: Int,
    val status: EventStatus
)
