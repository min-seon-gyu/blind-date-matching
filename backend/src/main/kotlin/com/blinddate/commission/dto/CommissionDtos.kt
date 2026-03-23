package com.blinddate.commission.dto

import com.blinddate.commission.entity.CommissionStatus
import java.time.LocalDateTime

data class CommissionResponse(
    val id: Long,
    val barId: Long,
    val barName: String,
    val eventId: Long,
    val eventTitle: String,
    val participantCount: Int,
    val eventPrice: Int,
    val commissionRate: Int,
    val unitPrice: Int,
    val totalAmount: Int,
    val status: CommissionStatus,
    val invoicedAt: LocalDateTime?,
    val paidAt: LocalDateTime?
)
