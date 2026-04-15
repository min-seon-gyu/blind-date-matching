package com.blinddate.matching.service

import com.blinddate.application.entity.Application
import com.blinddate.application.entity.ApplicationStatus
import com.blinddate.application.repository.ApplicationRepository
import com.blinddate.cafe.entity.Cafe
import com.blinddate.common.exception.BadRequestException
import com.blinddate.event.entity.Event
import com.blinddate.event.entity.EventStatus
import com.blinddate.event.repository.EventRepository
import com.blinddate.matching.entity.Choice
import com.blinddate.matching.entity.MatchResult
import com.blinddate.matching.entity.ParticipantNumber
import com.blinddate.matching.repository.ChoiceRepository
import com.blinddate.matching.repository.MatchResultRepository
import com.blinddate.matching.repository.ParticipantNumberRepository
import com.blinddate.organizer.entity.Organizer
import com.blinddate.participant.entity.Gender
import com.blinddate.participant.entity.Participant
import com.blinddate.participant.entity.ParticipantProfile
import com.blinddate.participant.repository.ParticipantProfileRepository
import com.blinddate.participant.repository.ParticipantRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Optional

class MatchingServiceTest {
    private val participantNumberRepo = mockk<ParticipantNumberRepository>()
    private val choiceRepo = mockk<ChoiceRepository>()
    private val matchResultRepo = mockk<MatchResultRepository>()
    private val eventRepo = mockk<EventRepository>()
    private val participantRepo = mockk<ParticipantRepository>()
    private val profileRepo = mockk<ParticipantProfileRepository>()
    private val applicationRepo = mockk<ApplicationRepository>()

    private val service = MatchingService(
        participantNumberRepo, choiceRepo, matchResultRepo,
        eventRepo, participantRepo, profileRepo, applicationRepo
    )

    private val cafe = Cafe(name = "Test", address = "addr", slug = "test")
    private val organizer = Organizer(name = "Org", phoneNumber = "010", email = "org@test.com", password = "pass")
    private val event = Event(cafe = cafe, organizer = organizer, title = "Friday", date = LocalDate.of(2026, 4, 3),
        time = LocalTime.of(19, 0), price = 30000, maleCapacity = 10, femaleCapacity = 10,
        maxChoices = 3, choiceDeadline = LocalDateTime.of(2026, 4, 3, 21, 0))

    private val male1 = Participant(kakaoId = "m1", nickname = "Male1")
    private val male2 = Participant(kakaoId = "m2", nickname = "Male2")
    private val female1 = Participant(kakaoId = "f1", nickname = "Female1")
    private val female2 = Participant(kakaoId = "f2", nickname = "Female2")

    @Test
    fun `submitChoices should throw if exceeds maxChoices`() {
        every { applicationRepo.findByParticipantIdAndEventId(any(), any()) } returns Optional.of(
            Application(participant = male1, event = event, status = ApplicationStatus.APPROVED)
        )
        every { eventRepo.findById(any()) } returns Optional.of(event)
        every { profileRepo.findByParticipantId(any()) } returns Optional.of(
            ParticipantProfile(participant = male1, name = "M", age = 28, gender = Gender.MALE, job = "dev")
        )

        assertThrows(BadRequestException::class.java) {
            service.submitChoices(event.id, male1.id, listOf(1L, 2L, 3L, 4L))
        }
    }

    @Test
    fun `processMatching should match bidirectional choices`() {
        // male1 chose female1, female1 chose male1 -> match
        // male1 chose female2, female2 did NOT choose male1 -> no match
        val maleProfile = ParticipantProfile(participant = male1, name = "M", age = 28, gender = Gender.MALE, job = "dev")
        val femaleProfile = ParticipantProfile(participant = female1, name = "F", age = 26, gender = Gender.FEMALE, job = "design")

        val choices = listOf(
            Choice(event = event, chooser = male1, chosen = female1),
            Choice(event = event, chooser = male1, chosen = female2),
            Choice(event = event, chooser = female1, chosen = male1)
        )

        every { choiceRepo.findByEventId(event.id) } returns choices
        every { profileRepo.findByParticipantId(male1.id) } returns Optional.of(maleProfile)
        every { profileRepo.findByParticipantId(female1.id) } returns Optional.of(femaleProfile)
        every { matchResultRepo.existsByEventIdAndMember1IdAndMember2Id(any(), any(), any()) } returns false
        every { eventRepo.findById(event.id) } returns Optional.of(event)
        every { participantRepo.findById(male1.id) } returns Optional.of(male1)
        every { participantRepo.findById(female1.id) } returns Optional.of(female1)
        every { matchResultRepo.save(any()) } answers { firstArg() }

        service.processMatching(event.id)

        // Should save exactly 1 match (male1 <-> female1)
        verify(exactly = 1) { matchResultRepo.save(any()) }
    }

    @Test
    fun `getMatchResult should return matched partner`() {
        val result = MatchResult(event = event, member1 = male1, member2 = female1)
        every { matchResultRepo.findByEventIdAndParticipantId(event.id, male1.id) } returns listOf(result)

        val response = service.getMatchResult(event.id, male1.id)
        assertEquals(1, response.size)
        assertEquals("Female1", response[0].matchedNickname)
    }

    @Test
    fun `submitChoices succeeds with valid choices`() {
        // Use separate mock instances to avoid id=0 collision
        val maleProfile = ParticipantProfile(participant = male1, name = "M", age = 28, gender = Gender.MALE, job = "dev")
        val femaleProfile = ParticipantProfile(participant = female1, name = "F", age = 26, gender = Gender.FEMALE, job = "design")

        every { eventRepo.findById(event.id) } returns Optional.of(event)
        every { applicationRepo.findByParticipantIdAndEventId(any(), any()) } returns Optional.of(
            Application(participant = male1, event = event, status = ApplicationStatus.APPROVED)
        )
        // Since all ids are 0 (BaseEntity default), use returnsMany to control sequential calls
        every { profileRepo.findByParticipantId(any()) } returnsMany listOf(
            Optional.of(maleProfile),   // first call: getting myProfile (male)
            Optional.of(femaleProfile)   // second call: getting chosen profile (female)
        )
        every { participantRepo.findById(any()) } returnsMany listOf(
            Optional.of(male1),
            Optional.of(female1)
        )
        every { choiceRepo.deleteByEventIdAndChooserId(any(), any()) } just runs
        every { choiceRepo.saveAll(any<List<Choice>>()) } answers { firstArg() }

        assertDoesNotThrow {
            service.submitChoices(event.id, male1.id, listOf(female1.id))
        }
        verify { choiceRepo.saveAll(any<List<Choice>>()) }
    }

    @Test
    fun `submitChoices fails when deadline passed`() {
        val pastDeadlineEvent = Event(
            cafe = cafe, organizer = organizer, title = "Past", date = LocalDate.of(2026, 4, 3),
            time = LocalTime.of(19, 0), price = 30000, maleCapacity = 10, femaleCapacity = 10,
            maxChoices = 3, choiceDeadline = LocalDateTime.of(2020, 1, 1, 0, 0)
        )
        every { eventRepo.findById(any()) } returns Optional.of(pastDeadlineEvent)

        assertThrows(BadRequestException::class.java) {
            service.submitChoices(pastDeadlineEvent.id, male1.id, listOf(female1.id))
        }
    }

    @Test
    fun `processMatching with no choices produces no matches`() {
        // Empty choices -> no matches
        every { choiceRepo.findByEventId(event.id) } returns emptyList()

        service.processMatching(event.id)

        verify(exactly = 0) { matchResultRepo.save(any()) }
    }

    @Test
    fun `getParticipants returns opposite gender list`() {
        val maleProfile = ParticipantProfile(participant = male1, name = "M", age = 28, gender = Gender.MALE, job = "dev")
        val femaleProfile = ParticipantProfile(participant = female1, name = "F", age = 26, gender = Gender.FEMALE, job = "design")

        // First call to get myProfile (male), subsequent calls for opposite gender participants
        every { profileRepo.findByParticipantId(any()) } returnsMany listOf(
            Optional.of(maleProfile),
            Optional.of(femaleProfile)
        )

        val femaleNumber = ParticipantNumber(event = event, participant = female1, number = 1, gender = Gender.FEMALE)
        every { participantNumberRepo.findByEventIdAndGender(event.id, Gender.FEMALE) } returns listOf(femaleNumber)

        val result = service.getParticipants(event.id, male1.id)

        assertEquals(1, result.size)
        assertEquals("FEMALE", result[0].gender)
        assertEquals(1, result[0].number)
        assertEquals(26, result[0].age)
    }
}
