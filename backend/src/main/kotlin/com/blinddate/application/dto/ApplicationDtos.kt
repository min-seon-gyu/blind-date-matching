package com.blinddate.application.dto

import com.blinddate.application.entity.ApplicationStatus
import java.time.LocalDateTime

data class ApplicationResponse(
    val id: Long,
    val eventId: Long,
    val eventTitle: String,
    val participantId: Long,
    val participantNickname: String,
    val status: ApplicationStatus,
    val appliedAt: LocalDateTime,
    val reviewedAt: LocalDateTime?,
    val rejectReason: String?
)
