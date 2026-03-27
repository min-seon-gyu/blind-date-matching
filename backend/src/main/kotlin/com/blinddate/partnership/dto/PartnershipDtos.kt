package com.blinddate.partnership.dto

import com.blinddate.partnership.entity.Partnership
import com.blinddate.partnership.entity.PartnershipRequester
import com.blinddate.partnership.entity.PartnershipStatus
import java.time.LocalDateTime

data class PartnershipResponse(
    val id: Long,
    val cafeId: Long,
    val cafeName: String,
    val organizerId: Long,
    val organizerName: String,
    val status: PartnershipStatus,
    val requestedBy: PartnershipRequester,
    val message: String?,
    val respondedAt: LocalDateTime?,
    val terminatedAt: LocalDateTime?,
    val createdAt: LocalDateTime
)

data class RequestPartnershipRequest(
    val message: String? = null
)

fun Partnership.toResponse() = PartnershipResponse(
    id = id,
    cafeId = cafe.id,
    cafeName = cafe.name,
    organizerId = organizer.id,
    organizerName = organizer.name,
    status = status,
    requestedBy = requestedBy,
    message = message,
    respondedAt = respondedAt,
    terminatedAt = terminatedAt,
    createdAt = createdAt
)
