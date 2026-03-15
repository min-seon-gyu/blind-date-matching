package com.blinddate.auth.service

import com.blinddate.auth.dto.KakaoUserInfo
import com.blinddate.auth.jwt.JwtTokenProvider
import com.blinddate.auth.repository.RefreshTokenRepository
import com.blinddate.common.entity.BaseEntity
import com.blinddate.member.entity.Member
import com.blinddate.member.repository.MemberProfileRepository
import com.blinddate.member.repository.MemberRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional

class AuthServiceTest {
    private lateinit var authService: AuthService
    private val kakaoOAuthService = mockk<KakaoOAuthService>()
    private val memberRepository = mockk<MemberRepository>()
    private val memberProfileRepository = mockk<MemberProfileRepository>()
    private val jwtTokenProvider = mockk<JwtTokenProvider>()
    private val refreshTokenRepository = mockk<RefreshTokenRepository>(relaxed = true)

    @BeforeEach
    fun setUp() {
        authService = AuthService(kakaoOAuthService, memberRepository, memberProfileRepository, jwtTokenProvider, refreshTokenRepository)
    }

    private fun Member.setId(id: Long): Member {
        val f = BaseEntity::class.java.getDeclaredField("id"); f.isAccessible = true; f.set(this, id); return this
    }

    @Test
    fun `should login existing member`() {
        val member = Member(kakaoId = "123", email = "test@test.com", nickname = "test").setId(1L)
        every { kakaoOAuthService.getAccessToken("code123") } returns "kakao-token"
        every { kakaoOAuthService.getUserInfo("kakao-token") } returns KakaoUserInfo("123", "test@test.com", "test")
        every { memberRepository.findByKakaoId("123") } returns Optional.of(member)
        every { memberProfileRepository.existsByMemberId(1L) } returns true
        every { jwtTokenProvider.createAccessToken(1L, "USER") } returns "access-token"
        every { jwtTokenProvider.createRefreshToken(1L) } returns "refresh-token"

        val result = authService.kakaoLogin("code123")
        assertEquals("access-token", result.accessToken)
        assertFalse(result.isNewMember)
    }

    @Test
    fun `should register new member on first login`() {
        every { kakaoOAuthService.getAccessToken("code123") } returns "kakao-token"
        every { kakaoOAuthService.getUserInfo("kakao-token") } returns KakaoUserInfo("999", "new@test.com", "newbie")
        every { memberRepository.findByKakaoId("999") } returns Optional.empty()
        every { memberRepository.save(any()) } answers { firstArg<Member>().setId(2L) }
        every { memberProfileRepository.existsByMemberId(2L) } returns false
        every { jwtTokenProvider.createAccessToken(2L, "USER") } returns "access-token-new"
        every { jwtTokenProvider.createRefreshToken(2L) } returns "refresh-token-new"

        val result = authService.kakaoLogin("code123")
        assertEquals("access-token-new", result.accessToken)
        assertTrue(result.isNewMember)
        verify { memberRepository.save(any()) }
    }
}
