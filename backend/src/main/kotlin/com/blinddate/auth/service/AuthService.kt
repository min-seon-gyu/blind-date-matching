package com.blinddate.auth.service

import com.blinddate.auth.dto.TokenResponse
import com.blinddate.auth.jwt.JwtTokenProvider
import com.blinddate.auth.repository.RefreshTokenRepository
import com.blinddate.common.exception.UnauthorizedException
import com.blinddate.member.entity.Member
import com.blinddate.member.repository.MemberProfileRepository
import com.blinddate.member.repository.MemberRepository
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val kakaoOAuthService: KakaoOAuthService,
    private val memberRepository: MemberRepository,
    private val memberProfileRepository: MemberProfileRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val refreshTokenRepository: RefreshTokenRepository
) {
    fun kakaoLogin(code: String): TokenResponse {
        val kakaoAccessToken = kakaoOAuthService.getAccessToken(code)
        val userInfo = kakaoOAuthService.getUserInfo(kakaoAccessToken)
        val member = memberRepository.findByKakaoId(userInfo.id).orElseGet {
            memberRepository.save(Member(kakaoId = userInfo.id, email = userInfo.email, nickname = userInfo.nickname))
        }
        val hasProfile = memberProfileRepository.existsByMemberId(member.id)
        val accessToken = jwtTokenProvider.createAccessToken(member.id, member.role.name)
        val refreshToken = jwtTokenProvider.createRefreshToken(member.id)
        refreshTokenRepository.save(member.id, refreshToken, 1209600000)
        return TokenResponse(accessToken = accessToken, refreshToken = refreshToken, isNewMember = !hasProfile)
    }

    fun refresh(refreshToken: String): TokenResponse {
        if (!jwtTokenProvider.validateToken(refreshToken)) throw UnauthorizedException("유효하지 않은 리프레시 토큰입니다")
        val memberId = jwtTokenProvider.getMemberId(refreshToken)
        val stored = refreshTokenRepository.find(memberId) ?: throw UnauthorizedException("만료된 리프레시 토큰입니다")
        if (stored != refreshToken) throw UnauthorizedException("리프레시 토큰이 일치하지 않습니다")
        val member = memberRepository.findById(memberId).orElseThrow { UnauthorizedException("존재하지 않는 회원입니다") }
        val newAccessToken = jwtTokenProvider.createAccessToken(member.id, member.role.name)
        val newRefreshToken = jwtTokenProvider.createRefreshToken(member.id)
        refreshTokenRepository.delete(memberId)
        refreshTokenRepository.save(memberId, newRefreshToken, 1209600000)
        return TokenResponse(accessToken = newAccessToken, refreshToken = newRefreshToken, isNewMember = false)
    }

    fun logout(memberId: Long, accessToken: String) {
        refreshTokenRepository.delete(memberId)
    }
}
