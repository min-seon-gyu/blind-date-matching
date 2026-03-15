package com.blinddate.matching.service

import com.blinddate.application.entity.Application
import com.blinddate.application.entity.ApplicationStatus
import com.blinddate.application.repository.ApplicationRepository
import com.blinddate.common.entity.BaseEntity
import com.blinddate.common.exception.BadRequestException
import com.blinddate.event.entity.BlindDateEvent
import com.blinddate.event.repository.BlindDateEventRepository
import com.blinddate.matching.entity.Choice
import com.blinddate.matching.entity.MatchResult
import com.blinddate.matching.entity.ParticipantNumber
import com.blinddate.matching.repository.ChoiceRepository
import com.blinddate.matching.repository.MatchResultRepository
import com.blinddate.matching.repository.ParticipantNumberRepository
import com.blinddate.member.entity.Gender
import com.blinddate.member.entity.Member
import com.blinddate.member.entity.MemberProfile
import com.blinddate.member.repository.MemberProfileRepository
import com.blinddate.member.repository.MemberRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.LocalTime
import java.util.Optional

class MatchingServiceTest {

    private lateinit var matchingService: MatchingService
    private lateinit var participantNumberService: ParticipantNumberService

    private val participantNumberRepository = mockk<ParticipantNumberRepository>()
    private val choiceRepository = mockk<ChoiceRepository>()
    private val matchResultRepository = mockk<MatchResultRepository>()
    private val eventRepository = mockk<BlindDateEventRepository>()
    private val memberRepository = mockk<MemberRepository>()
    private val memberProfileRepository = mockk<MemberProfileRepository>()
    private val applicationRepository = mockk<ApplicationRepository>()

    @BeforeEach
    fun setUp() {
        matchingService = MatchingService(
            participantNumberRepository,
            choiceRepository,
            matchResultRepository,
            eventRepository,
            memberRepository,
            memberProfileRepository,
            applicationRepository
        )
        participantNumberService = ParticipantNumberService(
            participantNumberRepository,
            eventRepository,
            memberRepository
        )
    }

    private fun <T : BaseEntity> T.setId(id: Long): T {
        val f = BaseEntity::class.java.getDeclaredField("id")
        f.isAccessible = true
        f.set(this, id)
        return this
    }

    private fun createMember(id: Long, kakaoId: String = "kakao$id"): Member =
        Member(kakaoId = kakaoId, nickname = "user$id").setId(id)

    private fun createEvent(id: Long): BlindDateEvent = BlindDateEvent(
        title = "테스트 이벤트",
        date = LocalDate.of(2026, 5, 1),
        time = LocalTime.of(19, 0),
        maleCapacity = 5,
        femaleCapacity = 5,
        price = 30000,
        createdBy = createMember(99L)
    ).setId(id)

    private fun createProfile(member: Member, gender: Gender): MemberProfile =
        MemberProfile(
            member = member,
            name = "테스트",
            age = 25,
            gender = gender,
            job = "개발자"
        ).setId(member.id * 10)

    @Test
    fun `should assign participant numbers by gender`() {
        val event = createEvent(1L)
        val male1 = createMember(1L)
        val male2 = createMember(2L)

        every { eventRepository.findById(1L) } returns Optional.of(event)
        every { memberRepository.findById(1L) } returns Optional.of(male1)
        every { memberRepository.findById(2L) } returns Optional.of(male2)

        // First male: max is null, so next = 1
        every { participantNumberRepository.findMaxNumberByEventIdAndGender(1L, Gender.MALE) } returnsMany listOf(null, 1)
        every { participantNumberRepository.save(any()) } answers {
            val pn = firstArg<ParticipantNumber>()
            pn.setId(pn.number.toLong())
            pn
        }

        val result1 = participantNumberService.assignNumber(1L, 1L, Gender.MALE)
        val result2 = participantNumberService.assignNumber(1L, 2L, Gender.MALE)

        assertEquals(1, result1.number)
        assertEquals(2, result2.number)
        assertEquals(Gender.MALE, result1.gender)
        assertEquals(Gender.MALE, result2.gender)
    }

    @Test
    fun `should submit choices and process bidirectional match`() {
        val eventId = 1L
        val event = createEvent(eventId)
        val male = createMember(1L)
        val female = createMember(2L)
        val maleProfile = createProfile(male, Gender.MALE)
        val femaleProfile = createProfile(female, Gender.FEMALE)

        val maleApplication = Application(member = male, event = event, status = ApplicationStatus.APPROVED).setId(1L)

        // Setup for male submitting choices
        every { applicationRepository.findByMemberIdAndEventId(1L, eventId) } returns Optional.of(maleApplication)
        every { memberProfileRepository.findByMemberId(1L) } returns Optional.of(maleProfile)
        every { memberProfileRepository.findByMemberId(2L) } returns Optional.of(femaleProfile)
        every { eventRepository.findById(eventId) } returns Optional.of(event)
        every { memberRepository.findById(1L) } returns Optional.of(male)
        every { memberRepository.findById(2L) } returns Optional.of(female)
        every { choiceRepository.saveAll(any<List<Choice>>()) } returns emptyList()

        matchingService.submitChoices(eventId, 1L, listOf(2L))

        verify { choiceRepository.saveAll(any<List<Choice>>()) }

        // Now set up for processMatching
        val choiceMaleToFemale = Choice(event = event, chooser = male, chosen = female).setId(1L)
        val choiceFemaleToMale = Choice(event = event, chooser = female, chosen = male).setId(2L)

        every { choiceRepository.findByEventId(eventId) } returns listOf(choiceMaleToFemale, choiceFemaleToMale)
        every { matchResultRepository.existsByEventIdAndMember1IdAndMember2Id(eventId, 1L, 2L) } returns false
        every { matchResultRepository.save(any()) } answers { firstArg<MatchResult>().setId(1L) }

        matchingService.processMatching(eventId)

        verify { matchResultRepository.save(any()) }
    }

    @Test
    fun `should reject more than 3 choices`() {
        val eventId = 1L
        val memberId = 1L

        assertThrows<BadRequestException> {
            matchingService.submitChoices(eventId, memberId, listOf(2L, 3L, 4L, 5L))
        }
    }
}
