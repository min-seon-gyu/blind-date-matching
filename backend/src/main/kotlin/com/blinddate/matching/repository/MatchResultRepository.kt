package com.blinddate.matching.repository

import com.blinddate.matching.entity.MatchResult
import org.springframework.data.jpa.repository.JpaRepository

interface MatchResultRepository : JpaRepository<MatchResult, Long> {
    fun findByEventId(eventId: Long): List<MatchResult>
    fun findByEventIdAndMember1IdOrEventIdAndMember2Id(
        eventId1: Long, member1Id: Long,
        eventId2: Long, member2Id: Long
    ): List<MatchResult>
    fun existsByEventIdAndMember1IdAndMember2Id(eventId: Long, member1Id: Long, member2Id: Long): Boolean
    fun findByEventIdAndNotifiedFalse(eventId: Long): List<MatchResult>
}
