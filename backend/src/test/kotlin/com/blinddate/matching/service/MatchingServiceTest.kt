package com.blinddate.matching.service

import com.blinddate.application.entity.Application
import com.blinddate.application.entity.ApplicationStatus
import com.blinddate.application.repository.ApplicationRepository
import com.blinddate.bar.entity.Bar
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
import com.blinddate.participant.entity.Gender
import com.blinddate.participant.entity.Participant
import com.blinddate.participant.entity.ParticipantProfile
import com.blinddate.participant.repository.ParticipantProfileRepository
import com.blinddate.participant.repository.ParticipantRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
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

    private val bar = Bar(name = "Test", address = "addr", slug = "test")
    private val event = Event(bar = bar, title = "Friday", date = LocalDate.of(2026, 4, 3),
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
}
