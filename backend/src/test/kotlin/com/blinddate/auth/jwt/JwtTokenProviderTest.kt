package com.blinddate.auth.jwt

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class JwtTokenProviderTest {
    private lateinit var provider: JwtTokenProvider

    @BeforeEach
    fun setUp() {
        provider = JwtTokenProvider(
            secret = "test-secret-key-must-be-at-least-256-bits-long-for-hs256",
            accessTokenExpiry = 1800000,
            refreshTokenExpiry = 1209600000
        )
    }

    @Test
    fun `should create and validate participant token`() {
        val token = provider.createAccessToken(1L, UserType.PARTICIPANT)
        assertTrue(provider.validateToken(token))
        val principal = provider.getUserPrincipal(token)
        assertEquals(1L, principal.id)
        assertEquals(UserType.PARTICIPANT, principal.userType)
        assertNull(principal.cafeId)
    }

    @Test
    fun `should create bar owner token with cafeId`() {
        val token = provider.createAccessToken(2L, UserType.BAR_OWNER, cafeId = 10L)
        val principal = provider.getUserPrincipal(token)
        assertEquals(2L, principal.id)
        assertEquals(UserType.BAR_OWNER, principal.userType)
        assertEquals(10L, principal.cafeId)
    }

    @Test
    fun `should reject expired token`() {
        val expiredProvider = JwtTokenProvider(
            secret = "test-secret-key-must-be-at-least-256-bits-long-for-hs256",
            accessTokenExpiry = -1000,
            refreshTokenExpiry = -1000
        )
        val token = expiredProvider.createAccessToken(1L, UserType.PARTICIPANT)
        assertFalse(expiredProvider.validateToken(token))
    }

    @Test
    fun `should validate refresh token type`() {
        val accessToken = provider.createAccessToken(1L, UserType.PARTICIPANT)
        val refreshToken = provider.createRefreshToken(1L, UserType.PARTICIPANT)
        assertFalse(provider.validateRefreshToken(accessToken))
        assertTrue(provider.validateRefreshToken(refreshToken))
    }
}
