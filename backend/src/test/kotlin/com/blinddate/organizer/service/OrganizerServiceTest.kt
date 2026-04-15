package com.blinddate.organizer.service

import com.blinddate.cafe.entity.Cafe
import com.blinddate.cafe.repository.CafeRepository
import com.blinddate.common.exception.BadRequestException
import com.blinddate.common.exception.ForbiddenException
import com.blinddate.event.entity.Event
import com.blinddate.event.entity.EventStatus
import com.blinddate.event.entity.MatchingMode
import com.blinddate.event.repository.EventRepository
import com.blinddate.organizer.dto.CreateEventRequest
import com.blinddate.organizer.dto.UpdateEventRequest
import com.blinddate.organizer.entity.Organizer
import com.blinddate.organizer.repository.OrganizerRepository
import com.blinddate.partnership.service.PartnershipService
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import java.util.Optional

class OrganizerServiceTest {

    private val organizerRepo = mockk<OrganizerRepository>()
    private val cafeRepo = mockk<CafeRepository>()
    private val eventRepo = mockk<EventRepository>()
    private val partnershipService = mockk<PartnershipService>()
    private val service = OrganizerService(organizerRepo, cafeRepo, eventRepo, partnershipService)

    private val cafe = Cafe(name = "Test Cafe", address = "서울시 강남구", slug = "test-cafe")
    private val organizer = Organizer(name = "Org", phoneNumber = "010", email = "org@test.com", password = "pass")

    private val createRequest = CreateEventRequest(
        cafeId = 10L, title = "Friday Night", date = LocalDate.of(2026, 4, 1),
        time = LocalTime.of(19, 0), price = 30000, maleCapacity = 10, femaleCapacity = 10
    )

    @Test
    fun `createEvent succeeds when active partnership exists`() {
        every { partnershipService.hasActivePartnership(10L, 1L) } returns true
        every { organizerRepo.findById(1L) } returns Optional.of(organizer)
        every { cafeRepo.findById(10L) } returns Optional.of(cafe)
        every { eventRepo.save(any()) } answers { firstArg() }

        val result = service.createEvent(1L, createRequest)
        assertEquals("Friday Night", result.title)

        verify { partnershipService.hasActivePartnership(10L, 1L) }
    }

    @Test
    fun `createEvent fails when no active partnership exists`() {
        every { partnershipService.hasActivePartnership(10L, 1L) } returns false

        val ex = assertThrows(BadRequestException::class.java) {
            service.createEvent(1L, createRequest)
        }
        assertEquals("먼저 카페와 제휴를 맺어주세요", ex.message)
    }

    @Test
    fun `getMyEvents returns event list`() {
        val event = Event(
            cafe = cafe, organizer = organizer, title = "Friday",
            date = LocalDate.of(2026, 4, 3), time = LocalTime.of(19, 0),
            price = 30000, maleCapacity = 10, femaleCapacity = 10
        )
        every { eventRepo.findByOrganizerIdAndDeletedAtIsNull(organizer.id) } returns listOf(event)

        val result = service.getMyEvents(organizer.id)

        assertEquals(1, result.size)
        assertEquals("Friday", result[0].title)
    }

    @Test
    fun `updateEvent succeeds for own event`() {
        val event = Event(
            cafe = cafe, organizer = organizer, title = "Original",
            date = LocalDate.of(2026, 4, 3), time = LocalTime.of(19, 0),
            price = 30000, maleCapacity = 10, femaleCapacity = 10
        )
        every { eventRepo.findById(event.id) } returns Optional.of(event)

        val updateReq = UpdateEventRequest(
            title = "Updated", date = LocalDate.of(2026, 4, 5),
            time = LocalTime.of(20, 0), price = 35000,
            maleCapacity = 12, femaleCapacity = 12
        )

        val result = service.updateEvent(organizer.id, event.id, updateReq)

        assertEquals("Updated", result.title)
        assertEquals(35000, result.price)
    }

    @Test
    fun `updateEvent fails for wrong organizer`() {
        val otherOrganizer = Organizer(name = "Other", phoneNumber = "010", email = "other@test.com", password = "pass")
        val event = Event(
            cafe = cafe, organizer = organizer, title = "Friday",
            date = LocalDate.of(2026, 4, 3), time = LocalTime.of(19, 0),
            price = 30000, maleCapacity = 10, femaleCapacity = 10
        )
        every { eventRepo.findById(event.id) } returns Optional.of(event)

        val updateReq = UpdateEventRequest(
            title = "Hack", date = LocalDate.of(2026, 4, 5),
            time = LocalTime.of(20, 0), price = 0,
            maleCapacity = 10, femaleCapacity = 10
        )

        assertThrows(ForbiddenException::class.java) {
            service.updateEvent(99L, event.id, updateReq)
        }
    }

    @Test
    fun `deleteEvent succeeds with soft delete`() {
        val event = Event(
            cafe = cafe, organizer = organizer, title = "Friday",
            date = LocalDate.of(2026, 4, 3), time = LocalTime.of(19, 0),
            price = 30000, maleCapacity = 10, femaleCapacity = 10
        )
        every { eventRepo.findById(event.id) } returns Optional.of(event)

        service.deleteEvent(organizer.id, event.id)

        assertNotNull(event.deletedAt)
    }

    @Test
    fun `deleteEvent fails for wrong organizer`() {
        val event = Event(
            cafe = cafe, organizer = organizer, title = "Friday",
            date = LocalDate.of(2026, 4, 3), time = LocalTime.of(19, 0),
            price = 30000, maleCapacity = 10, femaleCapacity = 10
        )
        every { eventRepo.findById(event.id) } returns Optional.of(event)

        assertThrows(ForbiddenException::class.java) {
            service.deleteEvent(99L, event.id)
        }
    }

    @Test
    fun `closeEvent succeeds for OPEN event`() {
        val event = Event(
            cafe = cafe, organizer = organizer, title = "Friday",
            date = LocalDate.of(2026, 4, 3), time = LocalTime.of(19, 0),
            price = 30000, maleCapacity = 10, femaleCapacity = 10,
            status = EventStatus.OPEN
        )
        every { eventRepo.findById(event.id) } returns Optional.of(event)

        val result = service.closeEvent(organizer.id, event.id)

        assertEquals(EventStatus.CLOSED, result.status)
    }

    @Test
    fun `closeEvent fails for non-OPEN status`() {
        val event = Event(
            cafe = cafe, organizer = organizer, title = "Friday",
            date = LocalDate.of(2026, 4, 3), time = LocalTime.of(19, 0),
            price = 30000, maleCapacity = 10, femaleCapacity = 10,
            status = EventStatus.CLOSED
        )
        every { eventRepo.findById(event.id) } returns Optional.of(event)

        assertThrows(BadRequestException::class.java) {
            service.closeEvent(organizer.id, event.id)
        }
    }
}
