package com.blinddate.event.repository

import com.blinddate.event.entity.Event
import com.blinddate.event.entity.EventStatus
import org.springframework.data.jpa.repository.JpaRepository

interface EventRepository : JpaRepository<Event, Long> {
    fun findByBarIdAndDeletedAtIsNullOrderByDateAsc(barId: Long): List<Event>
    fun findByBarIdAndStatusAndDeletedAtIsNull(barId: Long, status: EventStatus): List<Event>
    fun findByStatus(status: EventStatus): List<Event>
}
