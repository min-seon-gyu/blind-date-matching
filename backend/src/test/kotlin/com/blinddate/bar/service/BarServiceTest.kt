package com.blinddate.bar.service

import com.blinddate.bar.dto.BarReserveRequest
import com.blinddate.bar.dto.BarStatusDto
import com.blinddate.bar.dto.CheckInRequest
import com.blinddate.bar.entity.Bar
import com.blinddate.bar.entity.BarReservation
import com.blinddate.bar.entity.BarReservationStatus
import com.blinddate.bar.entity.BarVisitLog
import com.blinddate.bar.repository.BarRepository
import com.blinddate.bar.repository.BarReservationRepository
import com.blinddate.bar.repository.BarVisitLogRepository
import com.blinddate.common.entity.BaseEntity
import com.blinddate.member.entity.Gender
import com.blinddate.member.entity.Member
import com.blinddate.member.repository.MemberRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Optional

class BarServiceTest {

    private lateinit var barService: BarService
    private val barRepository = mockk<BarRepository>()
    private val barReservationRepository = mockk<BarReservationRepository>()
    private val barVisitLogRepository = mockk<BarVisitLogRepository>()
    private val memberRepository = mockk<MemberRepository>()
    private val barStatusRedisService = mockk<BarStatusRedisService>()

    @BeforeEach
    fun setUp() {
        barService = BarService(
            barRepository,
            barReservationRepository,
            barVisitLogRepository,
            memberRepository,
            barStatusRedisService
        )
    }

    private fun <T : BaseEntity> T.setId(id: Long): T {
        val f = BaseEntity::class.java.getDeclaredField("id")
        f.isAccessible = true
        f.set(this, id)
        return this
    }

    private fun createBar(
        id: Long,
        isOpen: Boolean = true,
        totalSeats: Int = 20,
        currentMaleCount: Int = 0,
        currentFemaleCount: Int = 0
    ): Bar = Bar(
        name = "혼술바",
        address = "서울시 강남구",
        totalSeats = totalSeats,
        currentMaleCount = currentMaleCount,
        currentFemaleCount = currentFemaleCount,
        isOpen = isOpen,
        openTime = LocalTime.of(18, 0),
        closeTime = LocalTime.of(2, 0)
    ).setId(id)

    private fun createMember(id: Long): Member =
        Member(kakaoId = "kakao$id", nickname = "user$id").setId(id)

    private fun createReservation(
        id: Long,
        bar: Bar,
        member: Member,
        status: BarReservationStatus = BarReservationStatus.CONFIRMED
    ): BarReservation = BarReservation(
        bar = bar,
        member = member,
        date = LocalDate.of(2026, 3, 15),
        time = LocalTime.of(19, 0),
        status = status
    ).setId(id)

    private fun createVisitLog(
        id: Long,
        bar: Bar,
        member: Member?,
        gender: Gender,
        checkOutAt: LocalDateTime? = null
    ): BarVisitLog = BarVisitLog(
        bar = bar,
        member = member,
        gender = gender,
        checkInAt = LocalDateTime.now(),
        checkOutAt = checkOutAt
    ).setId(id)

    @Test
    fun `should return bar status with Redis counts`() {
        val bar = createBar(1L, totalSeats = 20)
        val redisStatus = BarStatusDto(maleCount = 3L, femaleCount = 2L)

        every { barRepository.findById(1L) } returns Optional.of(bar)
        every { barStatusRedisService.getStatus(1L) } returns redisStatus

        val result = barService.getStatus(1L)

        assertEquals(1L, result.id)
        assertEquals("혼술바", result.name)
        assertEquals(3L, result.currentMaleCount)
        assertEquals(2L, result.currentFemaleCount)
        assertEquals(15L, result.remainingSeats) // 20 - 3 - 2
        assertTrue(result.isOpen)
        verify { barStatusRedisService.getStatus(1L) }
    }

    @Test
    fun `should create reservation with CONFIRMED status`() {
        val bar = createBar(1L, isOpen = true)
        val member = createMember(1L)
        val request = BarReserveRequest(
            date = LocalDate.of(2026, 3, 15),
            time = LocalTime.of(19, 0)
        )

        every { barRepository.findById(1L) } returns Optional.of(bar)
        every { memberRepository.findById(1L) } returns Optional.of(member)
        every { barReservationRepository.save(any()) } answers {
            (firstArg() as BarReservation).also {
                val f = BaseEntity::class.java.getDeclaredField("id"); f.isAccessible = true; f.set(it, 1L)
            }
        }

        val result = barService.reserve(1L, 1L, request)

        assertEquals(BarReservationStatus.CONFIRMED, result.status)
        assertEquals(1L, result.barId)
        assertEquals(LocalDate.of(2026, 3, 15), result.date)
        verify { barReservationRepository.save(any()) }
    }

    @Test
    fun `should check in and increment counts`() {
        val bar = createBar(1L, isOpen = true, currentMaleCount = 0)
        val member = createMember(1L)
        val request = CheckInRequest(gender = Gender.MALE, memberId = 1L)

        every { barRepository.findById(1L) } returns Optional.of(bar)
        every { memberRepository.findById(1L) } returns Optional.of(member)
        every { barVisitLogRepository.save(any()) } answers {
            (firstArg() as BarVisitLog).also {
                val f = BaseEntity::class.java.getDeclaredField("id"); f.isAccessible = true; f.set(it, 1L)
            }
        }
        every { barStatusRedisService.incrementCount(1L, Gender.MALE) } just Runs

        barService.checkIn(1L, request)

        verify { barStatusRedisService.incrementCount(1L, Gender.MALE) }
        verify { barVisitLogRepository.save(any()) }
        assertEquals(1, bar.currentMaleCount)
    }

    @Test
    fun `should check out and decrement counts`() {
        val bar = createBar(1L, isOpen = true, currentMaleCount = 2)
        val member = createMember(1L)
        val visitLog = createVisitLog(1L, bar, member, Gender.MALE)

        every { barVisitLogRepository.findById(1L) } returns Optional.of(visitLog)
        every { barStatusRedisService.decrementCount(1L, Gender.MALE) } just Runs

        barService.checkOut(1L)

        assertNotNull(visitLog.checkOutAt)
        verify { barStatusRedisService.decrementCount(1L, Gender.MALE) }
        assertEquals(1, bar.currentMaleCount) // decremented from 2 to 1
    }
}
