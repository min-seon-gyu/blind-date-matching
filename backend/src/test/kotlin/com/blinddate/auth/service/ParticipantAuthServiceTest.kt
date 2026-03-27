package com.blinddate.auth.service

import com.blinddate.auth.jwt.JwtTokenProvider
import com.blinddate.auth.jwt.UserPrincipal
import com.blinddate.auth.jwt.UserType
import com.blinddate.common.exception.UnauthorizedException
import com.blinddate.participant.entity.Participant
import com.blinddate.participant.repository.ParticipantRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.Optional

class ParticipantAuthServiceTest {

    private val participantRepo = mockk<ParticipantRepository>()
    private val kakaoOAuthService = mockk<KakaoOAuthService>()
    private val jwtTokenProvider = mockk<JwtTokenProvider>()
    private val service = ParticipantAuthService(participantRepo, kakaoOAuthService, jwtTokenProvider)

    private val participant = Participant(kakaoId = "12345", nickname = "tester")

    @Test
    fun `kakaoLogin creates new participant if not exists`() {
        every { kakaoOAuthService.getAccessToken("code123") } returns "kakao-token"
        every { kakaoOAuthService.getUserInfo("kakao-token") } returns KakaoOAuthService.KakaoUserResponse(
            id = 12345L,
            kakao_account = KakaoOAuthService.KakaoAccount(
                email = "test@kakao.com",
                profile = KakaoOAuthService.KakaoProfile(nickname = "tester")
            )
        )
        every { participantRepo.existsByKakaoId("12345") } returns false
        every { participantRepo.findByKakaoId("12345") } returns Optional.empty()
        every { participantRepo.save(any()) } returns participant
        every { jwtTokenProvider.createAccessToken(participant.id, UserType.PARTICIPANT) } returns "access-token"
        every { jwtTokenProvider.createRefreshToken(participant.id, UserType.PARTICIPANT) } returns "refresh-token"

        val result = service.kakaoLogin("code123")

        assertTrue(result.isNewUser)
        assertEquals("access-token", result.accessToken)
        assertEquals("refresh-token", result.refreshToken)
        verify { participantRepo.save(any()) }
    }

    @Test
    fun `kakaoLogin returns existing participant if already registered`() {
        every { kakaoOAuthService.getAccessToken("code123") } returns "kakao-token"
        every { kakaoOAuthService.getUserInfo("kakao-token") } returns KakaoOAuthService.KakaoUserResponse(
            id = 12345L,
            kakao_account = KakaoOAuthService.KakaoAccount(
                email = "test@kakao.com",
                profile = KakaoOAuthService.KakaoProfile(nickname = "tester")
            )
        )
        every { participantRepo.existsByKakaoId("12345") } returns true
        every { participantRepo.findByKakaoId("12345") } returns Optional.of(participant)
        every { jwtTokenProvider.createAccessToken(participant.id, UserType.PARTICIPANT) } returns "access-token"
        every { jwtTokenProvider.createRefreshToken(participant.id, UserType.PARTICIPANT) } returns "refresh-token"

        val result = service.kakaoLogin("code123")

        assertFalse(result.isNewUser)
        assertEquals("access-token", result.accessToken)
    }

    @Test
    fun `refreshToken generates new tokens when valid`() {
        val principal = UserPrincipal(id = 1L, userType = UserType.PARTICIPANT)
        every { jwtTokenProvider.validateRefreshToken("valid-refresh") } returns true
        every { jwtTokenProvider.getUserPrincipal("valid-refresh") } returns principal
        every { jwtTokenProvider.createAccessToken(1L, UserType.PARTICIPANT, null) } returns "new-access"
        every { jwtTokenProvider.createRefreshToken(1L, UserType.PARTICIPANT) } returns "new-refresh"

        val result = service.refreshToken("valid-refresh")

        assertEquals("new-access", result.accessToken)
        assertEquals("new-refresh", result.refreshToken)
    }

    @Test
    fun `refreshToken throws when token is invalid`() {
        every { jwtTokenProvider.validateRefreshToken("invalid") } returns false

        assertThrows(UnauthorizedException::class.java) {
            service.refreshToken("invalid")
        }
    }
}
