package com.blinddate.application.service

import com.blinddate.application.entity.Application
import com.blinddate.application.entity.ApplicationStatus
import com.blinddate.application.repository.ApplicationRepository
import com.blinddate.common.entity.BaseEntity
import com.blinddate.common.exception.BadRequestException
import com.blinddate.common.exception.ConflictException
import com.blinddate.event.entity.BlindDateEvent
import com.blinddate.event.entity.EventStatus
import com.blinddate.event.repository.BlindDateEventRepository
import com.blinddate.member.entity.Gender
import com.blinddate.member.entity.Member
import com.blinddate.member.entity.MemberProfile
import com.blinddate.member.repository.MemberProfileRepository
import com.blinddate.member.repository.MemberRepository
import com.blinddate.payment.service.PaymentService
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.LocalTime
import java.util.Optional

class ApplicationServiceTest {

    private lateinit var applicationService: ApplicationService
    private val applicationRepository = mockk<ApplicationRepository>()
    private val eventRepository = mockk<BlindDateEventRepository>()
    private val memberRepository = mockk<MemberRepository>()
    private val memberProfileRepository = mockk<MemberProfileRepository>()
    private val paymentService = mockk<PaymentService>()

    @BeforeEach
    fun setUp() {
        applicationService = ApplicationService(
            applicationRepository, eventRepository, memberRepository, memberProfileRepository, paymentService
        )
    }

    private fun <T : BaseEntity> T.setId(id: Long): T {
        val f = BaseEntity::class.java.getDeclaredField("id")
        f.isAccessible = true
        f.set(this, id)
        return this
    }

    private fun createMember(id: Long, kakaoId: String = "kakao$id"): Member =
        Member(kakaoId = kakaoId, nickname = "user$id").setId(id)

    private fun createEvent(
        id: Long,
        maleCapacity: Int = 3,
        femaleCapacity: Int = 3,
        currentMaleCount: Int = 0,
        currentFemaleCount: Int = 0,
        status: EventStatus = EventStatus.OPEN,
        admin: Member = createMember(99L)
    ): BlindDateEvent = BlindDateEvent(
        title = "테스트 이벤트",
        date = LocalDate.of(2026, 5, 1),
        time = LocalTime.of(19, 0),
        maleCapacity = maleCapacity,
        femaleCapacity = femaleCapacity,
        currentMaleCount = currentMaleCount,
        currentFemaleCount = currentFemaleCount,
        price = 30000,
        status = status,
        createdBy = admin
    ).setId(id)

    private fun createProfile(member: Member, gender: Gender = Gender.MALE, age: Int = 25): MemberProfile =
        MemberProfile(
            member = member,
            name = "홍길동",
            age = age,
            gender = gender,
            job = "개발자"
        ).setId(member.id * 10)

    private fun createApplication(id: Long, member: Member, event: BlindDateEvent, status: ApplicationStatus = ApplicationStatus.PAYMENT_WAITING): Application =
        Application(member = member, event = event, status = status).setId(id)

    @Test
    fun `should apply to event`() {
        val member = createMember(1L)
        val event = createEvent(1L)
        val profile = createProfile(member, Gender.MALE)

        every { memberRepository.findById(1L) } returns Optional.of(member)
        every { eventRepository.findById(1L) } returns Optional.of(event)
        every { memberProfileRepository.findByMemberId(1L) } returns Optional.of(profile)
        every { applicationRepository.findByMemberIdAndEventId(1L, 1L) } returns Optional.empty()
        every { applicationRepository.save(any()) } answers {
            (firstArg() as Application).also {
                val f = BaseEntity::class.java.getDeclaredField("id"); f.isAccessible = true; f.set(it, 1L)
            }
        }

        val result = applicationService.apply(1L, 1L)

        assertEquals(ApplicationStatus.PAYMENT_WAITING, result.status)
        assertEquals(1L, result.eventId)
        assertEquals(1L, result.memberId)
        verify { applicationRepository.save(any()) }
    }

    @Test
    fun `should throw on duplicate application`() {
        val member = createMember(1L)
        val event = createEvent(1L)
        val existing = createApplication(1L, member, event)
        val profile = createProfile(member, Gender.MALE)

        every { memberRepository.findById(1L) } returns Optional.of(member)
        every { eventRepository.findById(1L) } returns Optional.of(event)
        every { memberProfileRepository.findByMemberId(1L) } returns Optional.of(profile)
        every { applicationRepository.findByMemberIdAndEventId(1L, 1L) } returns Optional.of(existing)

        assertThrows<ConflictException> {
            applicationService.apply(1L, 1L)
        }
    }

    @Test
    fun `should approve and increment count`() {
        val admin = createMember(99L)
        val member = createMember(1L)
        val event = createEvent(id = 1L, maleCapacity = 3, currentMaleCount = 0)
        val application = createApplication(1L, member, event, ApplicationStatus.PAID)
        val profile = createProfile(member, Gender.MALE)

        every { memberRepository.findById(99L) } returns Optional.of(admin)
        every { applicationRepository.findById(1L) } returns Optional.of(application)
        every { memberProfileRepository.findByMemberId(1L) } returns Optional.of(profile)
        every { eventRepository.findByIdForUpdate(1L) } returns Optional.of(event)

        val result = applicationService.approve(99L, 1L)

        assertEquals(ApplicationStatus.APPROVED, result.status)
        assertEquals(1, event.currentMaleCount)
        assertNotNull(application.reviewedAt)
        assertEquals(admin.id, application.reviewedBy?.id)
    }

    @Test
    fun `should reject on no available slots`() {
        val admin = createMember(99L)
        val member = createMember(1L)
        // All male slots taken
        val event = createEvent(id = 1L, maleCapacity = 3, currentMaleCount = 3)
        val application = createApplication(1L, member, event, ApplicationStatus.PAID)
        val profile = createProfile(member, Gender.MALE)

        every { memberRepository.findById(99L) } returns Optional.of(admin)
        every { applicationRepository.findById(1L) } returns Optional.of(application)
        every { memberProfileRepository.findByMemberId(1L) } returns Optional.of(profile)
        every { eventRepository.findByIdForUpdate(1L) } returns Optional.of(event)

        assertThrows<BadRequestException> {
            applicationService.approve(99L, 1L)
        }
    }
}
