package com.blinddate.application.repository

import com.blinddate.application.entity.Application
import com.blinddate.application.entity.ApplicationStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface ApplicationRepository : JpaRepository<Application, Long> {
    fun findByMemberIdAndEventId(memberId: Long, eventId: Long): Optional<Application>
    fun findByMemberId(memberId: Long): List<Application>
    fun findByEventId(eventId: Long): List<Application>
    fun findByEventIdAndStatus(eventId: Long, status: ApplicationStatus): List<Application>
}
