package com.blinddate.participant.repository
import com.blinddate.participant.entity.Participant
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
interface ParticipantRepository : JpaRepository<Participant, Long> {
    fun findByKakaoId(kakaoId: String): Optional<Participant>
    fun existsByKakaoId(kakaoId: String): Boolean
}
