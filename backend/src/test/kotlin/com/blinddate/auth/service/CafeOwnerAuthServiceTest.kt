package com.blinddate.auth.service

import com.blinddate.auth.jwt.JwtTokenProvider
import com.blinddate.auth.jwt.UserType
import com.blinddate.cafe.entity.Cafe
import com.blinddate.cafeowner.entity.CafeOwner
import com.blinddate.cafeowner.repository.CafeOwnerRepository
import com.blinddate.common.exception.UnauthorizedException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder

class CafeOwnerAuthServiceTest {

    private val cafeOwnerRepo = mockk<CafeOwnerRepository>()
    private val jwtTokenProvider = mockk<JwtTokenProvider>()
    private val passwordEncoder = mockk<PasswordEncoder>()
    private val service = CafeOwnerAuthService(cafeOwnerRepo, jwtTokenProvider, passwordEncoder)

    private val cafe = Cafe(name = "Test Cafe", address = "서울시 강남구", slug = "test-cafe")
    private val owner = CafeOwner(cafe = cafe, name = "Owner", phoneNumber = "010", email = "owner@test.com", password = "encoded")

    @Test
    fun `login succeeds with valid credentials`() {
        every { cafeOwnerRepo.findByEmail("owner@test.com") } returns owner
        every { passwordEncoder.matches("password", "encoded") } returns true
        every { jwtTokenProvider.createAccessToken(owner.id, UserType.CAFE_OWNER, cafeId = cafe.id) } returns "access-token"
        every { jwtTokenProvider.createRefreshToken(owner.id, UserType.CAFE_OWNER) } returns "refresh-token"

        val result = service.login("owner@test.com", "password")

        assertEquals("access-token", result.accessToken)
        assertEquals("refresh-token", result.refreshToken)
    }

    @Test
    fun `login fails with wrong password`() {
        every { cafeOwnerRepo.findByEmail("owner@test.com") } returns owner
        every { passwordEncoder.matches("wrong", "encoded") } returns false

        assertThrows(UnauthorizedException::class.java) {
            service.login("owner@test.com", "wrong")
        }
    }

    @Test
    fun `login fails with unknown email`() {
        every { cafeOwnerRepo.findByEmail("unknown@test.com") } returns null

        assertThrows(UnauthorizedException::class.java) {
            service.login("unknown@test.com", "password")
        }
    }
}
