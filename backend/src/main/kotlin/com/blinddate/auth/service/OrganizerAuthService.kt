package com.blinddate.auth.service

import com.blinddate.auth.dto.TokenResponse
import com.blinddate.auth.jwt.JwtTokenProvider
import com.blinddate.auth.jwt.UserType
import com.blinddate.common.exception.UnauthorizedException
import com.blinddate.organizer.repository.OrganizerRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class OrganizerAuthService(
    private val organizerRepository: OrganizerRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder
) {
    fun login(email: String, password: String): TokenResponse {
        val organizer = organizerRepository.findByEmail(email)
            ?: throw UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다")
        if (!passwordEncoder.matches(password, organizer.password)) {
            throw UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다")
        }
        return TokenResponse(
            accessToken = jwtTokenProvider.createAccessToken(organizer.id, UserType.ORGANIZER),
            refreshToken = jwtTokenProvider.createRefreshToken(organizer.id, UserType.ORGANIZER)
        )
    }
}
