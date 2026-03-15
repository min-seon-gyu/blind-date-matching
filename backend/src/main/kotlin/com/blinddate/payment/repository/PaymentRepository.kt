package com.blinddate.payment.repository

import com.blinddate.payment.entity.Payment
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface PaymentRepository : JpaRepository<Payment, Long> {
    fun findByApplicationId(applicationId: Long): Optional<Payment>
    fun findByOrderId(orderId: String): Optional<Payment>
}
