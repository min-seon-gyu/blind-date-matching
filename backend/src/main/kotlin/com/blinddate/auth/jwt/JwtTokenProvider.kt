package com.blinddate.auth.jwt

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.access-token-expiry}") private val accessTokenExpiry: Long,
    @Value("\${jwt.refresh-token-expiry}") private val refreshTokenExpiry: Long
) {
    private val key: SecretKey by lazy { Keys.hmacShaKeyFor(secret.toByteArray()) }

    fun createAccessToken(memberId: Long, role: String): String = createToken(memberId, role, accessTokenExpiry)
    fun createRefreshToken(memberId: Long): String = createToken(memberId, null, refreshTokenExpiry)

    private fun createToken(memberId: Long, role: String?, expiry: Long): String {
        val now = Date()
        val builder = Jwts.builder()
            .subject(memberId.toString())
            .issuedAt(now)
            .expiration(Date(now.time + expiry))
        if (role != null) builder.claim("role", role)
        return builder.signWith(key).compact()
    }

    fun validateToken(token: String): Boolean = try {
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token); true
    } catch (e: Exception) { false }

    fun getMemberId(token: String): Long =
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload.subject.toLong()

    fun getRole(token: String): String? =
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload["role"] as? String

    fun getExpiration(token: String): Date =
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload.expiration
}
