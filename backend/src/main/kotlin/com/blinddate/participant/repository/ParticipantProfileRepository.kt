package com.blinddate.participant.repository
import com.blinddate.participant.entity.ParticipantProfile
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
interface ParticipantProfileRepository : JpaRepository<ParticipantProfile, Long> {
    fun findByParticipantId(participantId: Long): Optional<ParticipantProfile>
    fun existsByParticipantId(participantId: Long): Boolean
}
