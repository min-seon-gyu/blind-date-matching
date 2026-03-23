package com.blinddate.auth.service

import com.blinddate.auth.dto.TokenResponse
import com.blinddate.auth.jwt.JwtTokenProvider
import com.blinddate.auth.jwt.UserType
import com.blinddate.common.exception.UnauthorizedException
import com.blinddate.participant.entity.Participant
import com.blinddate.participant.repository.ParticipantRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ParticipantAuthService(
    private val participantRepository: ParticipantRepository,
    private val kakaoOAuthService: KakaoOAuthService,
    private val jwtTokenProvider: JwtTokenProvider
) {
    @Transactional
    fun kakaoLogin(code: String): TokenResponse {
        val kakaoToken = kakaoOAuthService.getAccessToken(code)
        val userInfo = kakaoOAuthService.getUserInfo(kakaoToken)
        val kakaoId = userInfo.id.toString()

        val isNew = !participantRepository.existsByKakaoId(kakaoId)
        val participant = participantRepository.findByKakaoId(kakaoId).orElseGet {
            participantRepository.save(Participant(
                kakaoId = kakaoId,
                nickname = userInfo.kakao_account?.profile?.nickname ?: ""
            ))
        }

        return TokenResponse(
            accessToken = jwtTokenProvider.createAccessToken(participant.id, UserType.PARTICIPANT),
            refreshToken = jwtTokenProvider.createRefreshToken(participant.id, UserType.PARTICIPANT),
            isNewUser = isNew
        )
    }

    fun refreshToken(refreshToken: String): TokenResponse {
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw UnauthorizedException("유효하지 않은 리프레시 토큰입니다")
        }
        val principal = jwtTokenProvider.getUserPrincipal(refreshToken)
        return TokenResponse(
            accessToken = jwtTokenProvider.createAccessToken(principal.id, principal.userType, principal.barId),
            refreshToken = jwtTokenProvider.createRefreshToken(principal.id, principal.userType)
        )
    }
}
