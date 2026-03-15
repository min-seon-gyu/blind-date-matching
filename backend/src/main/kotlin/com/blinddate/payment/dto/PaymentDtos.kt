package com.blinddate.payment.dto

import com.blinddate.payment.entity.PaymentStatus
import java.time.LocalDateTime

data class PaymentConfirmRequest(
    val paymentKey: String,
    val orderId: String,
    val amount: Int
)

data class PaymentResponse(
    val id: Long,
    val applicationId: Long,
    val amount: Int,
    val paymentKey: String,
    val orderId: String,
    val status: PaymentStatus,
    val paidAt: LocalDateTime?,
    val refundedAt: LocalDateTime?
)
