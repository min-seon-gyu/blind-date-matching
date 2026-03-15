package com.blinddate.payment.controller

import com.blinddate.payment.dto.PaymentConfirmRequest
import com.blinddate.payment.dto.PaymentResponse
import com.blinddate.payment.service.PaymentService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/payments")
class PaymentController(private val paymentService: PaymentService) {

    private fun currentMemberId(): Long = SecurityContextHolder.getContext().authentication.principal as Long

    @PostMapping("/confirm")
    fun confirm(@RequestBody request: PaymentConfirmRequest): ResponseEntity<PaymentResponse> =
        ResponseEntity.ok(paymentService.confirm(currentMemberId(), request))

    @PostMapping("/webhook")
    fun webhook(@RequestBody body: Map<String, Any>): ResponseEntity<Void> {
        paymentService.handleWebhook(body)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/{applicationId}")
    fun getPayment(@PathVariable applicationId: Long): ResponseEntity<PaymentResponse> =
        ResponseEntity.ok(paymentService.getPayment(applicationId))
}
