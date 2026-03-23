package com.blinddate.participant.service

import com.blinddate.common.exception.ConflictException
import com.blinddate.common.exception.NotFoundException
import com.blinddate.participant.dto.ProfileCreateRequest
import com.blinddate.participant.entity.*
import com.blinddate.participant.repository.*
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.Optional

class ParticipantServiceTest {
    private val participantRepo = mockk<ParticipantRepository>()
    private val profileRepo = mockk<ParticipantProfileRepository>()
    private val service = ParticipantService(participantRepo, profileRepo)

    @Test
    fun `getMe should return participant info`() {
        val p = Participant(kakaoId = "123", nickname = "tester", isProfileComplete = true)
        every { participantRepo.findById(1L) } returns Optional.of(p)
        val result = service.getMe(1L)
        assertEquals("tester", result.nickname)
        assertTrue(result.isProfileComplete)
    }

    @Test
    fun `getMe should throw when not found`() {
        every { participantRepo.findById(1L) } returns Optional.empty()
        assertThrows(NotFoundException::class.java) { service.getMe(1L) }
    }

    @Test
    fun `createProfile should save and mark profile complete`() {
        val p = Participant(kakaoId = "123")
        every { participantRepo.findById(1L) } returns Optional.of(p)
        every { profileRepo.existsByParticipantId(1L) } returns false
        every { profileRepo.save(any()) } answers { firstArg() }

        val request = ProfileCreateRequest(name = "홍길동", age = 28, gender = Gender.MALE, job = "개발자")
        val result = service.createProfile(1L, request)
        assertEquals("홍길동", result.name)
        assertEquals(28, result.age)
        assertTrue(p.isProfileComplete)
    }

    @Test
    fun `createProfile should throw if profile exists`() {
        val p = Participant(kakaoId = "123")
        every { participantRepo.findById(1L) } returns Optional.of(p)
        every { profileRepo.existsByParticipantId(1L) } returns true
        assertThrows(ConflictException::class.java) {
            service.createProfile(1L, ProfileCreateRequest("a", 20, Gender.MALE, "b"))
        }
    }

    @Test
    fun `updateProfile should update fields`() {
        val p = Participant(kakaoId = "123")
        val profile = ParticipantProfile(participant = p, name = "old", age = 25, gender = Gender.MALE, job = "old")
        every { profileRepo.findByParticipantId(1L) } returns Optional.of(profile)

        val request = ProfileCreateRequest(name = "new", age = 30, gender = Gender.MALE, job = "new")
        val result = service.updateProfile(1L, request)
        assertEquals("new", result.name)
        assertEquals(30, result.age)
    }
}
