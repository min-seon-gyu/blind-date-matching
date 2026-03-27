package com.blinddate.event.service

import com.blinddate.cafe.repository.CafeRepository
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
    private val cafeRepository: CafeRepository
) {
    fun getEventsByCafe(slug: String): List<EventResponse> {
        val cafe = cafeRepository.findBySlug(slug) ?: throw NotFoundException("카페를 찾을 수 없습니다")
        return eventRepository.findByCafeIdAndDeletedAtIsNull(cafe.id).map { it.toResponse() }
    }

    fun getEvent(eventId: Long): EventResponse {
        val event = eventRepository.findById(eventId).orElseThrow { NotFoundException("이벤트를 찾을 수 없습니다") }
        if (event.isDeleted()) throw NotFoundException("삭제된 이벤트입니다")
        return event.toResponse()
    }

    fun getEventByCafeSlugAndId(slug: String, eventId: Long): EventResponse {
        val cafe = cafeRepository.findBySlug(slug) ?: throw NotFoundException("카페를 찾을 수 없습니다")
        val event = eventRepository.findById(eventId).orElseThrow { NotFoundException("이벤트를 찾을 수 없습니다") }
        if (event.isDeleted() || event.cafe.slug != cafe.slug) throw NotFoundException("이벤트를 찾을 수 없습니다")
        return event.toResponse()
    }

    fun getByCafeId(cafeId: Long): List<EventResponse> {
        return eventRepository.findByCafeIdAndDeletedAtIsNull(cafeId).map { it.toResponse() }
    }

    fun Event.toResponse() = EventResponse(
        id, cafe.id, organizer.id, title, date, time, maleCapacity, femaleCapacity,
        currentMaleCount, currentFemaleCount, price, status, description,
        choiceDeadline, matchNotificationTime, minAge, maxAge, maxChoices, matchingMode
    )
}
