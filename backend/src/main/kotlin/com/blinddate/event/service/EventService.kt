package com.blinddate.event.service

import com.blinddate.bar.repository.BarRepository
import com.blinddate.common.exception.NotFoundException
import com.blinddate.event.dto.*
import com.blinddate.event.entity.Event
import com.blinddate.event.repository.EventRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class EventService(
    private val eventRepository: EventRepository,
    private val barRepository: BarRepository
) {
    fun getEventsByBar(slug: String): List<EventResponse> {
        val bar = barRepository.findBySlug(slug).orElseThrow { NotFoundException("바를 찾을 수 없습니다") }
        return eventRepository.findByBarIdAndDeletedAtIsNullOrderByDateAsc(bar.id).map { it.toResponse() }
    }

    fun getEvent(eventId: Long): EventResponse {
        val event = eventRepository.findById(eventId).orElseThrow { NotFoundException("이벤트를 찾을 수 없습니다") }
        if (event.isDeleted()) throw NotFoundException("삭제된 이벤트입니다")
        return event.toResponse()
    }

    fun getEventByBarSlugAndId(slug: String, eventId: Long): EventResponse {
        val bar = barRepository.findBySlug(slug).orElseThrow { NotFoundException("바를 찾을 수 없습니다") }
        val event = eventRepository.findById(eventId).orElseThrow { NotFoundException("이벤트를 찾을 수 없습니다") }
        if (event.isDeleted() || event.bar.slug != bar.slug) throw NotFoundException("이벤트를 찾을 수 없습니다")
        return event.toResponse()
    }

    fun Event.toResponse() = EventResponse(
        id, bar.id, title, date, time, maleCapacity, femaleCapacity,
        currentMaleCount, currentFemaleCount, price, status, description,
        choiceDeadline, matchNotificationTime, minAge, maxAge, maxChoices, matchingMode
    )
}
