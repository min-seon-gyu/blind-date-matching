package com.blinddate.member.service

import com.blinddate.common.entity.BaseEntity
import com.blinddate.common.exception.ConflictException
import com.blinddate.member.dto.ProfileCreateRequest
import com.blinddate.member.entity.*
import com.blinddate.member.repository.MemberProfileRepository
import com.blinddate.member.repository.MemberRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional

class MemberServiceTest {
    private lateinit var memberService: MemberService
    private val memberRepository = mockk<MemberRepository>()
    private val profileRepository = mockk<MemberProfileRepository>()

    @BeforeEach
    fun setUp() { memberService = MemberService(memberRepository, profileRepository) }

    private fun Member.setId(id: Long): Member {
        val f = BaseEntity::class.java.getDeclaredField("id"); f.isAccessible = true; f.set(this, id); return this
    }

    @Test
    fun `should create profile`() {
        val member = Member(kakaoId = "123").setId(1L)
        every { memberRepository.findById(1L) } returns Optional.of(member)
        every { profileRepository.existsByMemberId(1L) } returns false
        every { profileRepository.save(any()) } answers { firstArg() }

        val request = ProfileCreateRequest(name = "테스트", age = 25, gender = Gender.MALE, job = "개발자")
        val result = memberService.createProfile(1L, request)
        assertEquals("테스트", result.name)
        verify { profileRepository.save(any()) }
    }

    @Test
    fun `should throw if profile already exists`() {
        val member = Member(kakaoId = "123").setId(1L)
        every { memberRepository.findById(1L) } returns Optional.of(member)
        every { profileRepository.existsByMemberId(1L) } returns true

        val request = ProfileCreateRequest(name = "테스트", age = 25, gender = Gender.MALE, job = "개발자")
        assertThrows<ConflictException> { memberService.createProfile(1L, request) }
    }
}
