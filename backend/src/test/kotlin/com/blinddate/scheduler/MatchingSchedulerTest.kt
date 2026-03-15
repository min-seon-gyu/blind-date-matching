package com.blinddate.scheduler

import com.blinddate.common.entity.BaseEntity
import com.blinddate.event.entity.BlindDateEvent
import com.blinddate.event.entity.EventStatus
import com.blinddate.event.repository.BlindDateEventRepository
import com.blinddate.matching.repository.MatchResultRepository
import com.blinddate.matching.service.MatchingService
import com.blinddate.member.entity.Member
import com.blinddate.notification.service.NotificationService
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class MatchingSchedulerTest {

    private lateinit var matchingScheduler: MatchingScheduler

    private val eventRepository = mockk<BlindDateEventRepository>()
    private val matchingService = mockk<MatchingService>()
    private val matchResultRepository = mockk<MatchResultRepository>()
    private val notificationService = mockk<NotificationService>()

    @BeforeEach
    fun setUp() {
        matchingScheduler = MatchingScheduler(
            eventRepository,
            matchingService,
            matchResultRepository,
            notificationService
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

    private fun createEvent(
        id: Long,
        status: EventStatus = EventStatus.OPEN,
        choiceDeadline: LocalDateTime? = LocalDateTime.now().minusHours(1)
    ): BlindDateEvent = BlindDateEvent(
        title = "테스트 이벤트",
        date = LocalDate.of(2026, 5, 1),
        time = LocalTime.of(19, 0),
        maleCapacity = 5,
        femaleCapacity = 5,
        price = 30000,
        status = status,
        choiceDeadline = choiceDeadline,
        createdBy = createMember(99L)
    ).setId(id)

    @Test
    fun `should process matching for events past deadline`() {
        val eventWithPastDeadline = createEvent(1L, choiceDeadline = LocalDateTime.now().minusHours(2))
        val eventWithFutureDeadline = createEvent(2L, choiceDeadline = LocalDateTime.now().plusHours(2))

        every { eventRepository.findByStatus(EventStatus.OPEN) } returns listOf(
            eventWithPastDeadline, eventWithFutureDeadline
        )
        every { eventRepository.findByStatus(EventStatus.CLOSED) } returns emptyList()
        every { matchingService.processMatching(1L) } just Runs
        every { matchResultRepository.findByEventIdAndNotifiedFalse(any()) } returns emptyList()

        matchingScheduler.processMatchingForClosedEvents()

        verify(exactly = 1) { matchingService.processMatching(1L) }
        verify(exactly = 0) { matchingService.processMatching(2L) }
    }

    @Test
    fun `should not process matching when no events past deadline`() {
        val eventWithFutureDeadline = createEvent(1L, choiceDeadline = LocalDateTime.now().plusHours(5))

        every { eventRepository.findByStatus(EventStatus.OPEN) } returns listOf(eventWithFutureDeadline)
        every { eventRepository.findByStatus(EventStatus.CLOSED) } returns emptyList()

        matchingScheduler.processMatchingForClosedEvents()

        verify(exactly = 0) { matchingService.processMatching(any()) }
    }
}
