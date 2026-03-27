package com.blinddate.auth.service

import com.blinddate.admin.entity.PlatformAdmin
import com.blinddate.admin.repository.PlatformAdminRepository
import com.blinddate.auth.jwt.JwtTokenProvider
import com.blinddate.auth.jwt.UserType
import com.blinddate.common.exception.UnauthorizedException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.Optional

class AdminAuthServiceTest {

    private val platformAdminRepo = mockk<PlatformAdminRepository>()
    private val jwtTokenProvider = mockk<JwtTokenProvider>()
    private val passwordEncoder = mockk<PasswordEncoder>()
    private val service = AdminAuthService(platformAdminRepo, jwtTokenProvider, passwordEncoder)

    private val admin = PlatformAdmin(email = "admin@test.com", password = "encoded", name = "Admin")

    @Test
    fun `login succeeds with valid credentials`() {
        every { platformAdminRepo.findByEmail("admin@test.com") } returns Optional.of(admin)
        every { passwordEncoder.matches("password", "encoded") } returns true
        every { jwtTokenProvider.createAccessToken(admin.id, UserType.PLATFORM_ADMIN) } returns "access-token"
        every { jwtTokenProvider.createRefreshToken(admin.id, UserType.PLATFORM_ADMIN) } returns "refresh-token"

        val result = service.login("admin@test.com", "password")

        assertEquals("access-token", result.accessToken)
        assertEquals("refresh-token", result.refreshToken)
    }

    @Test
    fun `login fails with wrong password`() {
        every { platformAdminRepo.findByEmail("admin@test.com") } returns Optional.of(admin)
        every { passwordEncoder.matches("wrong", "encoded") } returns false

        assertThrows(UnauthorizedException::class.java) {
            service.login("admin@test.com", "wrong")
        }
    }
}
