package com.blinddate.matching.repository

import com.blinddate.matching.entity.Choice
import org.springframework.data.jpa.repository.JpaRepository

interface ChoiceRepository : JpaRepository<Choice, Long> {
    fun findByEventId(eventId: Long): List<Choice>
    fun findByEventIdAndChooserId(eventId: Long, chooserId: Long): List<Choice>
    fun countByEventIdAndChooserId(eventId: Long, chooserId: Long): Long
}
