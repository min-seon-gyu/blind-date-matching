package com.blinddate.matching.repository

import com.blinddate.matching.entity.ParticipantNumber
import com.blinddate.participant.entity.Gender
import org.springframework.data.jpa.repository.JpaRepository

interface ParticipantNumberRepository : JpaRepository<ParticipantNumber, Long> {
    fun findByEventIdAndGender(eventId: Long, gender: Gender): List<ParticipantNumber>
    fun findByEventIdAndParticipantId(eventId: Long, participantId: Long): ParticipantNumber?
    fun existsByEventId(eventId: Long): Boolean
}
