package com.blinddate.application.dto

import com.blinddate.application.entity.ApplicationStatus
import java.time.LocalDateTime

data class ApplicationResponse(
    val id: Long,
    val eventId: Long,
    val eventTitle: String,
    val memberId: Long,
    val memberNickname: String,
    val status: ApplicationStatus,
    val appliedAt: LocalDateTime,
    val reviewedAt: LocalDateTime?,
    val rejectReason: String?
)

data class ApplicationDetailResponse(
    val id: Long,
    val eventId: Long,
    val eventTitle: String,
    val memberId: Long,
    val memberNickname: String,
    val memberName: String?,
    val memberAge: Int?,
    val memberGender: String?,
    val memberJob: String?,
    val memberPhotoUrl: String?,
    val status: ApplicationStatus,
    val appliedAt: LocalDateTime,
    val reviewedAt: LocalDateTime?,
    val rejectReason: String?
)

data class RejectRequest(
    val reason: String = ""
)
