package com.blinddate.payment.service

import com.blinddate.application.entity.Application
import com.blinddate.application.entity.ApplicationStatus
import com.blinddate.application.repository.ApplicationRepository
import com.blinddate.common.entity.BaseEntity
import com.blinddate.event.entity.BlindDateEvent
import com.blinddate.event.entity.EventStatus
import com.blinddate.member.entity.Member
import com.blinddate.member.repository.MemberRepository
import com.blinddate.payment.dto.PaymentConfirmRequest
import com.blinddate.payment.entity.Payment
import com.blinddate.payment.entity.PaymentStatus
import com.blinddate.payment.repository.PaymentRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import java.util.Optional

class PaymentServiceTest {

    private lateinit var paymentService: PaymentService
    private val paymentRepository = mockk<PaymentRepository>()
    private val applicationRepository = mockk<ApplicationRepository>()
    private val memberRepository = mockk<MemberRepository>()
    private val tossPaymentsClient = mockk<TossPaymentsClient>()

    @BeforeEach
    fun setUp() {
        paymentService = PaymentService(
            paymentRepository, applicationRepository, memberRepository, tossPaymentsClient
        )
    }

    private fun <T : BaseEntity> T.setId(id: Long): T {
        val f = BaseEntity::class.java.getDeclaredField("id")
        f.isAccessible = true
        f.set(this, id)
        return this
    }

    private fun createMember(id: Long): Member =
        Member(kakaoId = "kakao$id", nickname = "user$id").setId(id)

    private fun createEvent(id: Long, admin: Member): BlindDateEvent =
        BlindDateEvent(
            title = "테스트 이벤트",
            date = LocalDate.of(2026, 5, 1),
            time = LocalTime.of(19, 0),
            maleCapacity = 3,
            femaleCapacity = 3,
            price = 30000,
            status = EventStatus.OPEN,
            createdBy = admin
        ).setId(id)

    private fun createApplication(id: Long, member: Member, event: BlindDateEvent, status: ApplicationStatus = ApplicationStatus.PAYMENT_WAITING): Application =
        Application(member = member, event = event, status = status).setId(id)

    private fun createPayment(id: Long, application: Application, member: Member, status: PaymentStatus = PaymentStatus.PAID): Payment =
        Payment(
            application = application,
            member = member,
            amount = 30000,
            paymentKey = "paymentKey123",
            orderId = "BLIND_${application.id}_1234567890",
            status = status,
            paidAt = if (status == PaymentStatus.PAID) java.time.LocalDateTime.now() else null
        ).setId(id)

    @Test
    fun `should confirm payment and update application status`() {
        val member = createMember(1L)
        val admin = createMember(99L)
        val event = createEvent(1L, admin)
        val application = createApplication(1L, member, event, ApplicationStatus.PAYMENT_WAITING)

        val orderId = "BLIND_1_1234567890"
        val request = PaymentConfirmRequest(
            paymentKey = "paymentKey123",
            orderId = orderId,
            amount = 30000
        )

        every { applicationRepository.findById(1L) } returns Optional.of(application)
        every { memberRepository.findById(1L) } returns Optional.of(member)
        every { tossPaymentsClient.confirmPayment("paymentKey123", orderId, 30000) } returns mapOf("status" to "DONE")
        every { paymentRepository.save(any()) } answers {
            (firstArg() as Payment).also {
                val f = BaseEntity::class.java.getDeclaredField("id"); f.isAccessible = true; f.set(it, 1L)
            }
        }

        val result = paymentService.confirm(1L, request)

        assertEquals(PaymentStatus.PAID, result.status)
        assertEquals(ApplicationStatus.PAID, application.status)
        assertEquals(orderId, result.orderId)
        assertEquals(30000, result.amount)
        verify { tossPaymentsClient.confirmPayment("paymentKey123", orderId, 30000) }
        verify { paymentRepository.save(any()) }
    }

    @Test
    fun `should refund payment`() {
        val member = createMember(1L)
        val admin = createMember(99L)
        val event = createEvent(1L, admin)
        val application = createApplication(1L, member, event, ApplicationStatus.PAID)
        val payment = createPayment(1L, application, member, PaymentStatus.PAID)

        every { paymentRepository.findByApplicationId(1L) } returns Optional.of(payment)
        every { tossPaymentsClient.cancelPayment("paymentKey123", any()) } returns mapOf("status" to "CANCELED")

        paymentService.refund(1L)

        assertEquals(PaymentStatus.REFUNDED, payment.status)
        assertNotNull(payment.refundedAt)
        verify { tossPaymentsClient.cancelPayment("paymentKey123", any()) }
    }
}
