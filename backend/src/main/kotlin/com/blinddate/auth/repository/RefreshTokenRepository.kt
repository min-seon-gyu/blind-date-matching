package com.blinddate.auth.repository

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit

@Repository
class RefreshTokenRepository(private val redisTemplate: RedisTemplate<String, String>) {
    private val prefix = "auth:refresh:"
    fun save(memberId: Long, refreshToken: String, expiryMs: Long) {
        redisTemplate.opsForValue().set("$prefix$memberId", refreshToken, expiryMs, TimeUnit.MILLISECONDS)
    }
    fun find(memberId: Long): String? = redisTemplate.opsForValue().get("$prefix$memberId")
    fun delete(memberId: Long) { redisTemplate.delete("$prefix$memberId") }
}
