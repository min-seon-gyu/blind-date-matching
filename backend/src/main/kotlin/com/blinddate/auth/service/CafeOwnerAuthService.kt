package com.blinddate.auth.service

import com.blinddate.auth.dto.TokenResponse
import com.blinddate.auth.jwt.JwtTokenProvider
import com.blinddate.auth.jwt.UserType
import com.blinddate.cafeowner.repository.CafeOwnerRepository
import com.blinddate.common.exception.NotFoundException
import com.blinddate.common.exception.UnauthorizedException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class CafeOwnerAuthService(
    private val cafeOwnerRepository: CafeOwnerRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder
) {
    fun login(email: String, password: String): TokenResponse {
        val owner = cafeOwnerRepository.findByEmail(email)
            ?: throw UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다")
        if (!passwordEncoder.matches(password, owner.password)) {
            throw UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다")
        }
        return TokenResponse(
            accessToken = jwtTokenProvider.createAccessToken(owner.id, UserType.CAFE_OWNER, cafeId = owner.cafe.id),
            refreshToken = jwtTokenProvider.createRefreshToken(owner.id, UserType.CAFE_OWNER)
        )
    }
}
