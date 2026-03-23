package com.blinddate.auth.service

import com.blinddate.auth.jwt.JwtTokenProvider
import com.blinddate.barowner.entity.BarOwner
import com.blinddate.barowner.repository.BarOwnerRepository
import com.blinddate.bar.entity.Bar
import com.blinddate.common.exception.UnauthorizedException
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.Optional

class BarOwnerAuthServiceTest {
    private val barOwnerRepository = mockk<BarOwnerRepository>()
    private val jwtTokenProvider = JwtTokenProvider(
        secret = "test-secret-key-must-be-at-least-256-bits-long-for-hs256",
        accessTokenExpiry = 1800000,
        refreshTokenExpiry = 1209600000
    )
    private val passwordEncoder = BCryptPasswordEncoder()
    private val service = BarOwnerAuthService(barOwnerRepository, jwtTokenProvider, passwordEncoder)

    @Test
    fun `login should return tokens for valid credentials`() {
        val bar = Bar(name = "Test Bar", address = "addr", slug = "test-bar")
        val owner = BarOwner(
            bar = bar, name = "Owner", phoneNumber = "010",
            email = "owner@test.com", password = passwordEncoder.encode("pass123")
        )
        every { barOwnerRepository.findByEmail("owner@test.com") } returns Optional.of(owner)

        val result = service.login("owner@test.com", "pass123")
        assertNotNull(result.accessToken)
        assertNotNull(result.refreshToken)
    }

    @Test
    fun `login should throw for wrong password`() {
        val bar = Bar(name = "Test Bar", address = "addr", slug = "test-bar")
        val owner = BarOwner(
            bar = bar, name = "Owner", phoneNumber = "010",
            email = "owner@test.com", password = passwordEncoder.encode("pass123")
        )
        every { barOwnerRepository.findByEmail("owner@test.com") } returns Optional.of(owner)

        assertThrows(UnauthorizedException::class.java) {
            service.login("owner@test.com", "wrong")
        }
    }

    @Test
    fun `login should throw for non-existent email`() {
        every { barOwnerRepository.findByEmail("unknown@test.com") } returns Optional.empty()

        assertThrows(UnauthorizedException::class.java) {
            service.login("unknown@test.com", "pass")
        }
    }
}
