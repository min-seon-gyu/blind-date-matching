package com.blinddate.auth.service

import com.blinddate.auth.dto.TokenResponse
import com.blinddate.auth.jwt.JwtTokenProvider
import com.blinddate.auth.jwt.UserType
import com.blinddate.barowner.repository.BarOwnerRepository
import com.blinddate.common.exception.UnauthorizedException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class BarOwnerAuthService(
    private val barOwnerRepository: BarOwnerRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder
) {
    fun login(email: String, password: String): TokenResponse {
        val owner = barOwnerRepository.findByEmail(email)
            .orElseThrow { UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다") }
        if (!passwordEncoder.matches(password, owner.password)) {
            throw UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다")
        }
        return TokenResponse(
            accessToken = jwtTokenProvider.createAccessToken(owner.id, UserType.BAR_OWNER, barId = owner.bar.id),
            refreshToken = jwtTokenProvider.createRefreshToken(owner.id, UserType.BAR_OWNER)
        )
    }
}
