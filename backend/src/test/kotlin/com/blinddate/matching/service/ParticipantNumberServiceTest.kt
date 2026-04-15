package com.blinddate.matching.service

import com.blinddate.application.entity.Application
import com.blinddate.application.entity.ApplicationStatus
import com.blinddate.application.repository.ApplicationRepository
import com.blinddate.cafe.entity.Cafe
import com.blinddate.event.entity.Event
import com.blinddate.event.repository.EventRepository
import com.blinddate.matching.repository.ParticipantNumberRepository
import com.blinddate.organizer.entity.Organizer
import com.blinddate.participant.entity.Gender
import com.blinddate.participant.entity.Participant
import com.blinddate.participant.entity.ParticipantProfile
import com.blinddate.participant.repository.ParticipantProfileRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import java.util.Optional

class ParticipantNumberServiceTest {

    private val participantNumberRepo = mockk<ParticipantNumberRepository>()
    private val applicationRepo = mockk<ApplicationRepository>()
    private val profileRepo = mockk<ParticipantProfileRepository>()
    private val eventRepo = mockk<EventRepository>()
    private val service = ParticipantNumberService(participantNumberRepo, applicationRepo, profileRepo, eventRepo)

    private val cafe = Cafe(name = "Test", address = "addr", slug = "test")
    private val organizer = Organizer(name = "Org", phoneNumber = "010", email = "org@test.com", password = "pass")
    private val event = Event(
        cafe = cafe, organizer = organizer, title = "Friday",
        date = LocalDate.of(2026, 4, 3), time = LocalTime.of(19, 0),
        price = 30000, maleCapacity = 10, femaleCapacity = 10
    )

    private val male1 = Participant(kakaoId = "m1", nickname = "Male1")
    private val male2 = Participant(kakaoId = "m2", nickname = "Male2")
    private val female1 = Participant(kakaoId = "f1", nickname = "Female1")

    @Test
    fun `assignNumbers assigns sequential numbers by gender`() {
        every { participantNumberRepo.existsByEventId(event.id) } returns false
        every { eventRepo.findById(event.id) } returns Optional.of(event)

        val app1 = Application(participant = male1, event = event, status = ApplicationStatus.APPROVED)
        val app2 = Application(participant = female1, event = event, status = ApplicationStatus.APPROVED)
        val app3 = Application(participant = male2, event = event, status = ApplicationStatus.APPROVED)

        every { applicationRepo.findByEventIdAndStatus(event.id, ApplicationStatus.APPROVED) } returns listOf(app1, app2, app3)
        every { profileRepo.findByParticipantId(male1.id) } returns Optional.of(
            ParticipantProfile(participant = male1, name = "M1", age = 28, gender = Gender.MALE, job = "dev")
        )
        every { profileRepo.findByParticipantId(female1.id) } returns Optional.of(
            ParticipantProfile(participant = female1, name = "F1", age = 26, gender = Gender.FEMALE, job = "design")
        )
        every { profileRepo.findByParticipantId(male2.id) } returns Optional.of(
            ParticipantProfile(participant = male2, name = "M2", age = 30, gender = Gender.MALE, job = "pm")
        )
        every { participantNumberRepo.save(any()) } answers { firstArg() }

        service.assignNumbers(event.id)

        // 3 participants with profiles -> 3 saves
        verify(exactly = 3) { participantNumberRepo.save(any()) }
    }

    @Test
    fun `assignNumbers only for APPROVED participants`() {
        every { participantNumberRepo.existsByEventId(event.id) } returns false
        every { eventRepo.findById(event.id) } returns Optional.of(event)

        // Only approved apps returned by repo
        val approvedApp = Application(participant = male1, event = event, status = ApplicationStatus.APPROVED)
        every { applicationRepo.findByEventIdAndStatus(event.id, ApplicationStatus.APPROVED) } returns listOf(approvedApp)
        every { profileRepo.findByParticipantId(male1.id) } returns Optional.of(
            ParticipantProfile(participant = male1, name = "M1", age = 28, gender = Gender.MALE, job = "dev")
        )
        every { participantNumberRepo.save(any()) } answers { firstArg() }

        service.assignNumbers(event.id)

        verify(exactly = 1) { participantNumberRepo.save(any()) }
    }

    @Test
    fun `assignNumbers skips when already assigned`() {
        every { participantNumberRepo.existsByEventId(event.id) } returns true

        service.assignNumbers(event.id)

        verify(exactly = 0) { applicationRepo.findByEventIdAndStatus(any(), any()) }
    }

    @Test
    fun `assignNumbers skips participant without profile`() {
        every { participantNumberRepo.existsByEventId(event.id) } returns false
        every { eventRepo.findById(event.id) } returns Optional.of(event)

        val appNoProfile = Application(participant = male1, event = event, status = ApplicationStatus.APPROVED)

        every { applicationRepo.findByEventIdAndStatus(event.id, ApplicationStatus.APPROVED) } returns listOf(appNoProfile)
        every { profileRepo.findByParticipantId(any()) } returns Optional.empty()

        service.assignNumbers(event.id)

        // No profile found, so no numbers should be saved
        verify(exactly = 0) { participantNumberRepo.save(any()) }
    }
}
