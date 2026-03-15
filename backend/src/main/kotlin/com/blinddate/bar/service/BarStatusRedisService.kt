package com.blinddate.bar.service

import com.blinddate.bar.dto.BarStatusDto
import com.blinddate.member.entity.Gender
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class BarStatusRedisService(
    private val redisTemplate: RedisTemplate<String, String>
) {
    companion object {
        private const val MALE_FIELD = "male_count"
        private const val FEMALE_FIELD = "female_count"

        fun redisKey(barId: Long) = "bar:status:$barId"
    }

    fun incrementCount(barId: Long, gender: Gender) {
        val field = if (gender == Gender.MALE) MALE_FIELD else FEMALE_FIELD
        redisTemplate.opsForHash<String, String>().increment(redisKey(barId), field, 1L)
    }

    fun decrementCount(barId: Long, gender: Gender) {
        val field = if (gender == Gender.MALE) MALE_FIELD else FEMALE_FIELD
        val hashOps = redisTemplate.opsForHash<String, String>()
        val current = hashOps.get(redisKey(barId), field)?.toLongOrNull() ?: 0L
        if (current > 0) {
            hashOps.increment(redisKey(barId), field, -1L)
        }
    }

    fun getStatus(barId: Long): BarStatusDto {
        val hashOps = redisTemplate.opsForHash<String, String>()
        val key = redisKey(barId)
        val maleCount = hashOps.get(key, MALE_FIELD)?.toLongOrNull() ?: 0L
        val femaleCount = hashOps.get(key, FEMALE_FIELD)?.toLongOrNull() ?: 0L
        return BarStatusDto(maleCount = maleCount, femaleCount = femaleCount)
    }

    fun resetCounts(barId: Long) {
        redisTemplate.delete(redisKey(barId))
    }
}
