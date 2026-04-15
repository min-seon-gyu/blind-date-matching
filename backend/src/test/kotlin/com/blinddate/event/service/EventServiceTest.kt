package com.blinddate.event.service

import com.blinddate.cafe.entity.Cafe
import com.blinddate.cafe.repository.CafeRepository
import com.blinddate.common.exception.NotFoundException
import com.blinddate.event.entity.Event
import com.blinddate.event.entity.EventStatus
import com.blinddate.event.repository.EventRepository
import com.blinddate.organizer.entity.Organizer
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Optional

class EventServiceTest {
    private val eventRepo = mockk<EventRepository>()
    private val cafeRepo = mockk<CafeRepository>()
    private val service = EventService(eventRepo, cafeRepo)

    private val organizer = Organizer(name = "Org", phoneNumber = "010", email = "org@test.com", password = "pass")

    @Test
    fun `getEventsByCafe should return events for given cafe slug`() {
        val cafe = Cafe(name = "Test", address = "addr", slug = "test-cafe")
        val event = Event(cafe = cafe, organizer = organizer, title = "Friday", date = LocalDate.of(2026, 4, 1),
            time = LocalTime.of(19, 0), price = 30000, maleCapacity = 10, femaleCapacity = 10)

        every { cafeRepo.findBySlug("test-cafe") } returns cafe
        every { eventRepo.findByCafeIdAndDeletedAtIsNull(cafe.id) } returns listOf(event)

        val result = service.getEventsByCafe("test-cafe")
        assertEquals(1, result.size)
        assertEquals("Friday", result[0].title)
    }

    @Test
    fun `getEventsByCafe should throw for unknown slug`() {
        every { cafeRepo.findBySlug("unknown") } returns null
        assertThrows(NotFoundException::class.java) { service.getEventsByCafe("unknown") }
    }

    @Test
    fun `getEvent should throw for deleted event`() {
        val cafe = Cafe(name = "Test", address = "addr", slug = "test-cafe")
        val event = Event(cafe = cafe, organizer = organizer, title = "Old", date = LocalDate.of(2026, 4, 1),
            time = LocalTime.of(19, 0), price = 30000, maleCapacity = 10, femaleCapacity = 10,
            deletedAt = LocalDateTime.now())
        every { eventRepo.findById(1L) } returns Optional.of(event)
        assertThrows(NotFoundException::class.java) { service.getEvent(1L) }
    }

    @Test
    fun `getEventByCafeSlugAndId should validate cafe ownership`() {
        val cafe = Cafe(name = "Test", address = "addr", slug = "test-cafe")
        val otherCafe = Cafe(name = "Other", address = "addr2", slug = "other-cafe")
        val event = Event(cafe = otherCafe, organizer = organizer, title = "Friday", date = LocalDate.of(2026, 4, 1),
            time = LocalTime.of(19, 0), price = 30000, maleCapacity = 10, femaleCapacity = 10)

        every { cafeRepo.findBySlug("test-cafe") } returns cafe
        every { eventRepo.findById(1L) } returns Optional.of(event)

        assertThrows(NotFoundException::class.java) { service.getEventByCafeSlugAndId("test-cafe", 1L) }
    }
}
