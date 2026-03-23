package com.blinddate.commission.dto

import com.blinddate.commission.entity.CommissionStatus
import com.blinddate.commission.entity.CommissionTargetType
import java.time.LocalDateTime

data class CommissionResponse(
    val id: Long,
    val cafeId: Long,
    val cafeName: String,
    val eventId: Long,
    val eventTitle: String,
    val targetType: CommissionTargetType,
    val targetId: Long,
    val participantCount: Int,
    val eventPrice: Int,
    val commissionRate: Int,
    val unitPrice: Int,
    val totalAmount: Int,
    val status: CommissionStatus,
    val invoicedAt: LocalDateTime?,
    val paidAt: LocalDateTime?
)
