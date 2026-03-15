package com.blinddate.matching.repository

import com.blinddate.matching.entity.ParticipantNumber
import com.blinddate.member.entity.Gender
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ParticipantNumberRepository : JpaRepository<ParticipantNumber, Long> {
    fun findByEventIdAndGender(eventId: Long, gender: Gender): List<ParticipantNumber>
    fun findByEventId(eventId: Long): List<ParticipantNumber>
    fun findByEventIdAndMemberId(eventId: Long, memberId: Long): ParticipantNumber?

    @Query("SELECT MAX(pn.number) FROM ParticipantNumber pn WHERE pn.event.id = :eventId AND pn.gender = :gender")
    fun findMaxNumberByEventIdAndGender(eventId: Long, gender: Gender): Int?
}
