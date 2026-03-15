package com.blinddate.auth.jwt

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class JwtTokenProviderTest {
    private lateinit var provider: JwtTokenProvider

    @BeforeEach
    fun setUp() {
        provider = JwtTokenProvider(
            secret = "test-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm",
            accessTokenExpiry = 1800000,
            refreshTokenExpiry = 1209600000
        )
    }

    @Test
    fun `should create and validate access token`() {
        val token = provider.createAccessToken(1L, "USER")
        assertTrue(provider.validateToken(token))
        assertEquals(1L, provider.getMemberId(token))
        assertEquals("USER", provider.getRole(token))
    }

    @Test
    fun `should create refresh token`() {
        val token = provider.createRefreshToken(1L)
        assertTrue(provider.validateToken(token))
        assertEquals(1L, provider.getMemberId(token))
    }

    @Test
    fun `should fail validation for invalid token`() {
        assertFalse(provider.validateToken("invalid.token.here"))
    }
}
