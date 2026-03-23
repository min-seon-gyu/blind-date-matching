package com.blinddate.application.repository

import com.blinddate.application.entity.Application
import com.blinddate.application.entity.ApplicationStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface ApplicationRepository : JpaRepository<Application, Long> {
    fun findByParticipantIdAndEventId(participantId: Long, eventId: Long): Optional<Application>
    fun findByParticipantId(participantId: Long): List<Application>
    fun findByEventId(eventId: Long): List<Application>
    fun findByEventIdAndStatus(eventId: Long, status: ApplicationStatus): List<Application>
    fun countByEventIdAndStatus(eventId: Long, status: ApplicationStatus): Long
}
