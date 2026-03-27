package com.blinddate.auth.service

import com.blinddate.auth.jwt.JwtTokenProvider
import com.blinddate.auth.jwt.UserType
import com.blinddate.common.exception.UnauthorizedException
import com.blinddate.organizer.entity.Organizer
import com.blinddate.organizer.repository.OrganizerRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder

class OrganizerAuthServiceTest {

    private val organizerRepo = mockk<OrganizerRepository>()
    private val jwtTokenProvider = mockk<JwtTokenProvider>()
    private val passwordEncoder = mockk<PasswordEncoder>()
    private val service = OrganizerAuthService(organizerRepo, jwtTokenProvider, passwordEncoder)

    private val organizer = Organizer(name = "Org", phoneNumber = "010", email = "org@test.com", password = "encoded")

    @Test
    fun `login succeeds with valid credentials`() {
        every { organizerRepo.findByEmail("org@test.com") } returns organizer
        every { passwordEncoder.matches("password", "encoded") } returns true
        every { jwtTokenProvider.createAccessToken(organizer.id, UserType.ORGANIZER) } returns "access-token"
        every { jwtTokenProvider.createRefreshToken(organizer.id, UserType.ORGANIZER) } returns "refresh-token"

        val result = service.login("org@test.com", "password")

        assertEquals("access-token", result.accessToken)
        assertEquals("refresh-token", result.refreshToken)
    }

    @Test
    fun `login fails with wrong password`() {
        every { organizerRepo.findByEmail("org@test.com") } returns organizer
        every { passwordEncoder.matches("wrong", "encoded") } returns false

        assertThrows(UnauthorizedException::class.java) {
            service.login("org@test.com", "wrong")
        }
    }

    @Test
    fun `login fails with unknown email`() {
        every { organizerRepo.findByEmail("unknown@test.com") } returns null

        assertThrows(UnauthorizedException::class.java) {
            service.login("unknown@test.com", "password")
        }
    }
}
