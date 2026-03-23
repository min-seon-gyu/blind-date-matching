package com.blinddate.barowner.service

import com.blinddate.bar.entity.Bar
import com.blinddate.bar.repository.BarRepository
import com.blinddate.barowner.dto.BarUpdateRequest
import com.blinddate.common.exception.ForbiddenException
import com.blinddate.common.exception.NotFoundException
import com.blinddate.event.dto.EventCreateRequest
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

class BarOwnerServiceTest {
    private val barRepo = mockk<BarRepository>()
    private val eventRepo = mockk<EventRepository>()
    private val service = BarOwnerService(barRepo, eventRepo)

    private val bar = Bar(name = "Test Bar", address = "addr", slug = "test-bar")

    @Test
    fun `getMyBar should return bar info`() {
        every { barRepo.findById(bar.id) } returns Optional.of(bar)
        val result = service.getMyBar(bar.id)
        assertEquals("Test Bar", result.name)
    }

    @Test
    fun `updateMyBar should update fields`() {
        every { barRepo.findById(bar.id) } returns Optional.of(bar)
        val request = BarUpdateRequest(name = "New Name", address = "new addr", description = "desc")
        val result = service.updateMyBar(bar.id, request)
        assertEquals("New Name", result.name)
    }

    @Test
    fun `createEvent should create event for bar`() {
        every { barRepo.findById(bar.id) } returns Optional.of(bar)
        every { eventRepo.save(any()) } answers { firstArg() }

        val request = EventCreateRequest(
            title = "Friday Night", date = LocalDate.of(2026, 4, 3),
            time = LocalTime.of(19, 0), maleCapacity = 10, femaleCapacity = 10, price = 30000
        )
        val result = service.createEvent(bar.id, request)
        assertEquals("Friday Night", result.title)
        assertEquals(bar.id, result.barId)
    }

    @Test
    fun `deleteEvent should soft delete`() {
        val event = Event(bar = bar, title = "Old", date = LocalDate.of(2026, 4, 1),
            time = LocalTime.of(19, 0), price = 30000, maleCapacity = 10, femaleCapacity = 10)
        every { barRepo.findById(bar.id) } returns Optional.of(bar)
        every { eventRepo.findById(1L) } returns Optional.of(event)
        service.deleteEvent(bar.id, 1L)
        assertNotNull(event.deletedAt)
    }

    @Test
    fun `deleteEvent should throw for wrong bar`() {
        val otherBar = Bar(name = "Other", address = "addr2", slug = "other")
        val event = Event(bar = otherBar, title = "Old", date = LocalDate.of(2026, 4, 1),
            time = LocalTime.of(19, 0), price = 30000, maleCapacity = 10, femaleCapacity = 10)
        every { barRepo.findById(bar.id) } returns Optional.of(bar)
        every { eventRepo.findById(1L) } returns Optional.of(event)
        assertThrows(ForbiddenException::class.java) { service.deleteEvent(bar.id, 1L) }
    }

    @Test
    fun `closeEvent should transition OPEN to CLOSED`() {
        val event = Event(bar = bar, title = "Friday", date = LocalDate.of(2026, 4, 1),
            time = LocalTime.of(19, 0), price = 30000, maleCapacity = 10, femaleCapacity = 10,
            status = EventStatus.OPEN)
        every { barRepo.findById(bar.id) } returns Optional.of(bar)
        every { eventRepo.findById(1L) } returns Optional.of(event)
        service.closeEvent(bar.id, 1L)
        assertEquals(EventStatus.CLOSED, event.status)
    }
}
