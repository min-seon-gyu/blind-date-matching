package com.blinddate.auth.service

import com.blinddate.admin.repository.PlatformAdminRepository
import com.blinddate.auth.dto.TokenResponse
import com.blinddate.auth.jwt.JwtTokenProvider
import com.blinddate.auth.jwt.UserType
import com.blinddate.common.exception.UnauthorizedException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AdminAuthService(
    private val platformAdminRepository: PlatformAdminRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder
) {
    fun login(email: String, password: String): TokenResponse {
        val admin = platformAdminRepository.findByEmail(email)
            .orElseThrow { UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다") }
        if (!passwordEncoder.matches(password, admin.password)) {
            throw UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다")
        }
        return TokenResponse(
            accessToken = jwtTokenProvider.createAccessToken(admin.id, UserType.PLATFORM_ADMIN),
            refreshToken = jwtTokenProvider.createRefreshToken(admin.id, UserType.PLATFORM_ADMIN)
        )
    }
}
