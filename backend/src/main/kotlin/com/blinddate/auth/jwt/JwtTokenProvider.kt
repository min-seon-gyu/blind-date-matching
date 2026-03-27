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

    fun createAccessToken(userId: Long, userType: UserType, cafeId: Long? = null): String =
        buildToken(userId, userType, cafeId, accessTokenExpiry, "access")

    fun createRefreshToken(userId: Long, userType: UserType): String =
        buildToken(userId, userType, null, refreshTokenExpiry, "refresh")

    fun validateToken(token: String): Boolean = try {
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token)
        true
    } catch (e: Exception) {
        false
    }

    fun validateRefreshToken(token: String): Boolean =
        validateToken(token) && getTokenType(token) == "refresh"

    fun getUserPrincipal(token: String): UserPrincipal {
        val claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
        return UserPrincipal(
            id = claims.subject.toLong(),
            userType = UserType.valueOf(claims["userType"] as String),
            cafeId = (claims["cafeId"] as? Number)?.toLong()
        )
    }

    private fun getTokenType(token: String): String {
        val claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
        return claims["tokenType"] as? String ?: "access"
    }

    private fun buildToken(userId: Long, userType: UserType, cafeId: Long?, expiry: Long, tokenType: String): String {
        val now = Date()
        val builder = Jwts.builder()
            .subject(userId.toString())
            .claim("userType", userType.name)
            .claim("tokenType", tokenType)
            .issuedAt(now)
            .expiration(Date(now.time + expiry))
            .signWith(key)

        cafeId?.let { builder.claim("cafeId", it) }
        return builder.compact()
    }
}
