package com.blinddate.payment.service

import com.blinddate.application.entity.ApplicationStatus
import com.blinddate.application.repository.ApplicationRepository
import com.blinddate.common.exception.BadRequestException
import com.blinddate.common.exception.NotFoundException
import com.blinddate.member.repository.MemberRepository
import com.blinddate.payment.dto.PaymentConfirmRequest
import com.blinddate.payment.dto.PaymentResponse
import com.blinddate.payment.entity.Payment
import com.blinddate.payment.entity.PaymentStatus
import com.blinddate.payment.repository.PaymentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val applicationRepository: ApplicationRepository,
    private val memberRepository: MemberRepository,
    private val tossPaymentsClient: TossPaymentsClient
) {

    @Transactional
    fun confirm(memberId: Long, request: PaymentConfirmRequest): PaymentResponse {
        // orderId pattern: BLIND_{applicationId}_{timestamp}
        val applicationId = parseApplicationIdFromOrderId(request.orderId)

        val application = applicationRepository.findById(applicationId)
            .orElseThrow { NotFoundException("신청 내역을 찾을 수 없습니다") }

        if (application.member.id != memberId) {
            throw BadRequestException("본인의 결제만 처리할 수 있습니다")
        }

        if (application.status != ApplicationStatus.PAYMENT_WAITING) {
            throw BadRequestException("결제 대기 상태인 신청만 결제할 수 있습니다")
        }

        val member = memberRepository.findById(memberId)
            .orElseThrow { NotFoundException("회원을 찾을 수 없습니다") }

        // Call TossPayments API
        tossPaymentsClient.confirmPayment(request.paymentKey, request.orderId, request.amount)

        val payment = paymentRepository.save(
            Payment(
                application = application,
                member = member,
                amount = request.amount,
                paymentKey = request.paymentKey,
                orderId = request.orderId,
                status = PaymentStatus.PAID,
                paidAt = LocalDateTime.now()
            )
        )

        application.status = ApplicationStatus.PAID

        return payment.toResponse()
    }

    @Transactional
    fun refund(applicationId: Long) {
        val payment = paymentRepository.findByApplicationId(applicationId)
            .orElseThrow { NotFoundException("결제 정보를 찾을 수 없습니다") }

        if (payment.status != PaymentStatus.PAID) {
            throw BadRequestException("결제 완료 상태인 결제만 환불할 수 있습니다")
        }

        tossPaymentsClient.cancelPayment(payment.paymentKey, "신청 취소에 따른 환불")

        payment.status = PaymentStatus.REFUNDED
        payment.refundedAt = LocalDateTime.now()
    }

    @Transactional
    fun handleWebhook(body: Map<String, Any>) {
        val eventType = body["eventType"] as? String ?: return
        val data = body["data"] as? Map<*, *> ?: return

        when (eventType) {
            "PAYMENT_STATUS_CHANGED" -> {
                val orderId = data["orderId"] as? String ?: return
                val status = data["status"] as? String ?: return

                paymentRepository.findByOrderId(orderId).ifPresent { payment ->
                    when (status) {
                        "DONE" -> {
                            payment.status = PaymentStatus.PAID
                            payment.paidAt = LocalDateTime.now()
                        }
                        "CANCELED", "PARTIAL_CANCELED" -> {
                            payment.status = PaymentStatus.REFUNDED
                            payment.refundedAt = LocalDateTime.now()
                        }
                        "ABORTED", "EXPIRED" -> {
                            payment.status = PaymentStatus.FAILED
                        }
                    }
                }
            }
        }
    }

    fun getPayment(applicationId: Long): PaymentResponse {
        val payment = paymentRepository.findByApplicationId(applicationId)
            .orElseThrow { NotFoundException("결제 정보를 찾을 수 없습니다") }
        return payment.toResponse()
    }

    private fun parseApplicationIdFromOrderId(orderId: String): Long {
        // orderId format: BLIND_{applicationId}_{timestamp}
        return try {
            orderId.split("_")[1].toLong()
        } catch (e: Exception) {
            throw BadRequestException("잘못된 주문 ID 형식입니다: $orderId")
        }
    }

    private fun Payment.toResponse() = PaymentResponse(
        id = id,
        applicationId = application.id,
        amount = amount,
        paymentKey = paymentKey,
        orderId = orderId,
        status = status,
        paidAt = paidAt,
        refundedAt = refundedAt
    )
}
