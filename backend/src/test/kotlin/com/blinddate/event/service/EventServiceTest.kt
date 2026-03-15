package com.blinddate.event.service

import com.blinddate.common.entity.BaseEntity
import com.blinddate.event.dto.EventCreateRequest
import com.blinddate.event.entity.EventStatus
import com.blinddate.event.repository.BlindDateEventRepository
import com.blinddate.member.entity.Member
import com.blinddate.member.repository.MemberRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import java.util.Optional

class EventServiceTest {
    private lateinit var eventService: EventService
    private val eventRepository = mockk<BlindDateEventRepository>()
    private val memberRepository = mockk<MemberRepository>()

    @BeforeEach
    fun setUp() { eventService = EventService(eventRepository, memberRepository) }

    private fun Member.setId(id: Long): Member {
        val f = BaseEntity::class.java.getDeclaredField("id"); f.isAccessible = true; f.set(this, id); return this
    }

    @Test
    fun `should list events by month`() {
        every { eventRepository.findByYearAndMonth(2026, 3) } returns emptyList()
        assertTrue(eventService.getEventsByMonth(2026, 3).isEmpty())
    }

    @Test
    fun `should create event`() {
        val admin = Member(kakaoId = "admin").setId(1L)
        every { memberRepository.findById(1L) } returns Optional.of(admin)
        every { eventRepository.save(any()) } answers { firstArg() }

        val request = EventCreateRequest(
            title = "테스트 이벤트", date = LocalDate.of(2026, 3, 21),
            time = LocalTime.of(19, 0), maleCapacity = 3, femaleCapacity = 3, price = 30000
        )
        val result = eventService.createEvent(1L, request)
        assertEquals("테스트 이벤트", result.title)
        assertEquals(EventStatus.OPEN, result.status)
    }
}
