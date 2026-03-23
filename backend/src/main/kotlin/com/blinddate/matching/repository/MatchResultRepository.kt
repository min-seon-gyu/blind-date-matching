package com.blinddate.matching.repository

import com.blinddate.matching.entity.MatchResult
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface MatchResultRepository : JpaRepository<MatchResult, Long> {
    fun findByEventId(eventId: Long): List<MatchResult>
    fun findByEventIdAndNotifiedFalse(eventId: Long): List<MatchResult>
    @Query("SELECT m FROM MatchResult m WHERE m.event.id = :eventId AND (m.member1.id = :participantId OR m.member2.id = :participantId)")
    fun findByEventIdAndParticipantId(eventId: Long, participantId: Long): List<MatchResult>
    fun existsByEventIdAndMember1IdAndMember2Id(eventId: Long, member1Id: Long, member2Id: Long): Boolean
}
