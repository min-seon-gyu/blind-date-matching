package com.blinddate.event.service

import com.blinddate.bar.entity.Bar
import com.blinddate.bar.repository.BarRepository
import com.blinddate.common.exception.NotFoundException
import com.blinddate.event.entity.Event
import com.blinddate.event.entity.EventStatus
import com.blinddate.event.repository.EventRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Optional

class EventServiceTest {
    private val eventRepo = mockk<EventRepository>()
    private val barRepo = mockk<BarRepository>()
    private val service = EventService(eventRepo, barRepo)

    @Test
    fun `getEventsByBar should return events for given bar slug`() {
        val bar = Bar(name = "Test", address = "addr", slug = "test-bar")
        val event = Event(bar = bar, title = "Friday", date = LocalDate.of(2026, 4, 1),
            time = LocalTime.of(19, 0), price = 30000, maleCapacity = 10, femaleCapacity = 10)

        every { barRepo.findBySlug("test-bar") } returns Optional.of(bar)
        every { eventRepo.findByBarIdAndDeletedAtIsNullOrderByDateAsc(bar.id) } returns listOf(event)

        val result = service.getEventsByBar("test-bar")
        assertEquals(1, result.size)
        assertEquals("Friday", result[0].title)
    }

    @Test
    fun `getEventsByBar should throw for unknown slug`() {
        every { barRepo.findBySlug("unknown") } returns Optional.empty()
        assertThrows(NotFoundException::class.java) { service.getEventsByBar("unknown") }
    }

    @Test
    fun `getEvent should throw for deleted event`() {
        val bar = Bar(name = "Test", address = "addr", slug = "test-bar")
        val event = Event(bar = bar, title = "Old", date = LocalDate.of(2026, 4, 1),
            time = LocalTime.of(19, 0), price = 30000, maleCapacity = 10, femaleCapacity = 10,
            deletedAt = LocalDateTime.now())
        every { eventRepo.findById(1L) } returns Optional.of(event)
        assertThrows(NotFoundException::class.java) { service.getEvent(1L) }
    }

    @Test
    fun `getEventByBarSlugAndId should validate bar ownership`() {
        val bar = Bar(name = "Test", address = "addr", slug = "test-bar")
        val otherBar = Bar(name = "Other", address = "addr2", slug = "other-bar")
        val event = Event(bar = otherBar, title = "Friday", date = LocalDate.of(2026, 4, 1),
            time = LocalTime.of(19, 0), price = 30000, maleCapacity = 10, femaleCapacity = 10)

        every { barRepo.findBySlug("test-bar") } returns Optional.of(bar)
        every { eventRepo.findById(1L) } returns Optional.of(event)

        assertThrows(NotFoundException::class.java) { service.getEventByBarSlugAndId("test-bar", 1L) }
    }
}
