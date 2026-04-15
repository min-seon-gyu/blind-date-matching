package com.blinddate.application.service

import com.blinddate.application.entity.Application
import com.blinddate.application.entity.ApplicationStatus
import com.blinddate.application.repository.ApplicationRepository
import com.blinddate.cafe.entity.Cafe
import com.blinddate.common.exception.BadRequestException
import com.blinddate.common.exception.ConflictException
import com.blinddate.common.exception.ForbiddenException
import com.blinddate.common.exception.NotFoundException
import com.blinddate.event.entity.Event
import com.blinddate.event.entity.EventStatus
import com.blinddate.event.repository.EventRepository
import com.blinddate.organizer.entity.Organizer
import com.blinddate.organizer.repository.OrganizerRepository
import com.blinddate.participant.entity.*
import com.blinddate.participant.repository.ParticipantProfileRepository
import com.blinddate.participant.repository.ParticipantRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import java.util.Optional

class ApplicationServiceTest {
    private val applicationRepo = mockk<ApplicationRepository>()
    private val eventRepo = mockk<EventRepository>()
    private val participantRepo = mockk<ParticipantRepository>()
    private val profileRepo = mockk<ParticipantProfileRepository>()
    private val organizerRepo = mockk<OrganizerRepository>()
    private val service = ApplicationService(applicationRepo, eventRepo, participantRepo, profileRepo, organizerRepo)

    private val cafe = Cafe(name = "Test", address = "addr", slug = "test")
    private val organizer = Organizer(name = "Org", phoneNumber = "010", email = "org@test.com", password = "pass")
    private val participant = Participant(kakaoId = "123", nickname = "tester", isProfileComplete = true)
    private val profile = ParticipantProfile(participant = participant, name = "홍길동", age = 28, gender = Gender.MALE, job = "dev")
    private val event = Event(cafe = cafe, organizer = organizer, title = "Friday", date = LocalDate.of(2026, 4, 3),
        time = LocalTime.of(19, 0), price = 30000, maleCapacity = 10, femaleCapacity = 10)

    @Test
    fun `apply should create application`() {
        every { participantRepo.findById(1L) } returns Optional.of(participant)
        every { eventRepo.findById(1L) } returns Optional.of(event)
        every { profileRepo.findByParticipantId(1L) } returns Optional.of(profile)
        every { applicationRepo.findByParticipantIdAndEventId(1L, 1L) } returns Optional.empty()
        every { applicationRepo.save(any()) } answers { firstArg() }

        val result = service.apply(1L, 1L)
        assertEquals(ApplicationStatus.PENDING, result.status)
    }

    @Test
    fun `apply should throw for non-OPEN event`() {
        val closedEvent = Event(cafe = cafe, organizer = organizer, title = "Old", date = LocalDate.of(2026, 4, 1),
            time = LocalTime.of(19, 0), price = 30000, maleCapacity = 10, femaleCapacity = 10,
            status = EventStatus.CLOSED)
        every { participantRepo.findById(1L) } returns Optional.of(participant)
        every { eventRepo.findById(1L) } returns Optional.of(closedEvent)
        every { profileRepo.findByParticipantId(1L) } returns Optional.of(profile)

        assertThrows(BadRequestException::class.java) { service.apply(1L, 1L) }
    }

    @Test
    fun `apply should throw for duplicate application`() {
        every { participantRepo.findById(1L) } returns Optional.of(participant)
        every { eventRepo.findById(1L) } returns Optional.of(event)
        every { profileRepo.findByParticipantId(1L) } returns Optional.of(profile)
        every { applicationRepo.findByParticipantIdAndEventId(1L, 1L) } returns Optional.of(mockk())

        assertThrows(ConflictException::class.java) { service.apply(1L, 1L) }
    }

    @Test
    fun `apply should throw if profile incomplete`() {
        every { participantRepo.findById(1L) } returns Optional.of(participant)
        every { eventRepo.findById(1L) } returns Optional.of(event)
        every { profileRepo.findByParticipantId(1L) } returns Optional.empty()

        assertThrows(BadRequestException::class.java) { service.apply(1L, 1L) }
    }

    @Test
    fun `apply should throw for age restriction`() {
        val ageEvent = Event(cafe = cafe, organizer = organizer, title = "25+", date = LocalDate.of(2026, 4, 3),
            time = LocalTime.of(19, 0), price = 30000, maleCapacity = 10, femaleCapacity = 10,
            minAge = 30)
        every { participantRepo.findById(1L) } returns Optional.of(participant)
        every { eventRepo.findById(1L) } returns Optional.of(ageEvent)
        every { profileRepo.findByParticipantId(1L) } returns Optional.of(profile) // age 28

        assertThrows(BadRequestException::class.java) { service.apply(1L, 1L) }
    }

    @Test
    fun `cancel should change status to CANCELLED`() {
        val app = Application(participant = participant, event = event, status = ApplicationStatus.PENDING)
        every { applicationRepo.findById(1L) } returns Optional.of(app)

        service.cancel(participant.id, 1L)
        assertEquals(ApplicationStatus.CANCELLED, app.status)
    }

    @Test
    fun `approve should increment male count`() {
        val app = Application(participant = participant, event = event, status = ApplicationStatus.PENDING)
        every { applicationRepo.findById(1L) } returns Optional.of(app)
        every { profileRepo.findByParticipantId(any()) } returns Optional.of(profile) // MALE
        every { organizerRepo.findById(organizer.id) } returns Optional.of(organizer)

        service.approve(organizer.id, 1L)
        assertEquals(ApplicationStatus.APPROVED, app.status)
        assertEquals(1, event.currentMaleCount)
    }

    @Test
    fun `approve should throw when capacity exceeded`() {
        val fullEvent = Event(cafe = cafe, organizer = organizer, title = "Full", date = LocalDate.of(2026, 4, 3),
            time = LocalTime.of(19, 0), price = 30000, maleCapacity = 0, femaleCapacity = 10)
        val app = Application(participant = participant, event = fullEvent, status = ApplicationStatus.PENDING)
        every { applicationRepo.findById(1L) } returns Optional.of(app)
        every { profileRepo.findByParticipantId(any()) } returns Optional.of(profile)
        assertThrows(BadRequestException::class.java) { service.approve(organizer.id, 1L) }
    }

    @Test
    fun `reject should set reason`() {
        val app = Application(participant = participant, event = event, status = ApplicationStatus.PENDING)
        every { applicationRepo.findById(1L) } returns Optional.of(app)
        every { organizerRepo.findById(organizer.id) } returns Optional.of(organizer)

        service.reject(organizer.id, 1L, "프로필 미흡")
        assertEquals(ApplicationStatus.REJECTED, app.status)
        assertEquals("프로필 미흡", app.rejectReason)
    }

    @Test
    fun `getByEventForOrganizer returns applications for own event`() {
        val app = Application(participant = participant, event = event, status = ApplicationStatus.PENDING)
        every { eventRepo.findById(event.id) } returns Optional.of(event)
        every { applicationRepo.findByEventId(event.id) } returns listOf(app)

        val result = service.getByEventForOrganizer(organizer.id, event.id)

        assertEquals(1, result.size)
        assertEquals(ApplicationStatus.PENDING, result[0].status)
    }

    @Test
    fun `cancel fails when status is not PENDING`() {
        val app = Application(participant = participant, event = event, status = ApplicationStatus.APPROVED)
        every { applicationRepo.findById(1L) } returns Optional.of(app)

        assertThrows(BadRequestException::class.java) {
            service.cancel(participant.id, 1L)
        }
    }

    @Test
    fun `approve fails when wrong organizer`() {
        val app = Application(participant = participant, event = event, status = ApplicationStatus.PENDING)
        every { applicationRepo.findById(1L) } returns Optional.of(app)

        assertThrows(ForbiddenException::class.java) {
            service.approve(99L, 1L)
        }
    }
}
