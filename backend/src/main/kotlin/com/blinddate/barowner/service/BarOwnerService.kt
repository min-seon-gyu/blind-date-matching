package com.blinddate.barowner.service

import com.blinddate.bar.dto.BarResponse
import com.blinddate.bar.entity.Bar
import com.blinddate.bar.repository.BarRepository
import com.blinddate.barowner.dto.BarUpdateRequest
import com.blinddate.barowner.dto.EventStatsResponse
import com.blinddate.common.exception.BadRequestException
import com.blinddate.common.exception.ForbiddenException
import com.blinddate.common.exception.NotFoundException
import com.blinddate.event.dto.EventCreateRequest
import com.blinddate.event.dto.EventResponse
import com.blinddate.event.entity.Event
import com.blinddate.event.entity.EventStatus
import com.blinddate.event.repository.EventRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class BarOwnerService(
    private val barRepository: BarRepository,
    private val eventRepository: EventRepository
) {
    fun getMyBar(barId: Long): BarResponse {
        val bar = barRepository.findById(barId).orElseThrow { NotFoundException("바를 찾을 수 없습니다") }
        return bar.toBarResponse()
    }

    @Transactional
    fun updateMyBar(barId: Long, request: BarUpdateRequest): BarResponse {
        val bar = barRepository.findById(barId).orElseThrow { NotFoundException("바를 찾을 수 없습니다") }
        bar.apply {
            name = request.name; address = request.address; description = request.description
            logoUrl = request.logoUrl; coverImageUrl = request.coverImageUrl
        }
        return bar.toBarResponse()
    }

    fun getMyEvents(barId: Long): List<EventResponse> {
        return eventRepository.findByBarIdAndDeletedAtIsNullOrderByDateAsc(barId).map { it.toEventResponse() }
    }

    @Transactional
    fun createEvent(barId: Long, request: EventCreateRequest): EventResponse {
        val bar = barRepository.findById(barId).orElseThrow { NotFoundException("바를 찾을 수 없습니다") }
        val event = eventRepository.save(Event(
            bar = bar, title = request.title, date = request.date, time = request.time,
            price = request.price, maleCapacity = request.maleCapacity, femaleCapacity = request.femaleCapacity,
            description = request.description, choiceDeadline = request.choiceDeadline,
            matchNotificationTime = request.matchNotificationTime, minAge = request.minAge,
            maxAge = request.maxAge, maxChoices = request.maxChoices, matchingMode = request.matchingMode
        ))
        return event.toEventResponse()
    }

    @Transactional
    fun updateEvent(barId: Long, eventId: Long, request: EventCreateRequest): EventResponse {
        val event = getOwnedEvent(barId, eventId)
        event.apply {
            title = request.title; date = request.date; time = request.time
            price = request.price; maleCapacity = request.maleCapacity; femaleCapacity = request.femaleCapacity
            description = request.description; choiceDeadline = request.choiceDeadline
            matchNotificationTime = request.matchNotificationTime; minAge = request.minAge
            maxAge = request.maxAge; maxChoices = request.maxChoices; matchingMode = request.matchingMode
        }
        return event.toEventResponse()
    }

    @Transactional
    fun deleteEvent(barId: Long, eventId: Long) {
        val event = getOwnedEvent(barId, eventId)
        event.deletedAt = LocalDateTime.now()
    }

    @Transactional
    fun closeEvent(barId: Long, eventId: Long): EventResponse {
        val event = getOwnedEvent(barId, eventId)
        if (event.status != EventStatus.OPEN) throw BadRequestException("OPEN 상태인 이벤트만 마감할 수 있습니다")
        event.status = EventStatus.CLOSED
        return event.toEventResponse()
    }

    private fun getOwnedEvent(barId: Long, eventId: Long): Event {
        val bar = barRepository.findById(barId).orElseThrow { NotFoundException("바를 찾을 수 없습니다") }
        val event = eventRepository.findById(eventId).orElseThrow { NotFoundException("이벤트를 찾을 수 없습니다") }
        if (event.isDeleted()) throw NotFoundException("삭제된 이벤트입니다")
        if (event.bar.slug != bar.slug) throw ForbiddenException("해당 이벤트에 대한 권한이 없습니다")
        return event
    }

    private fun Bar.toBarResponse() = BarResponse(id, name, address, description, logoUrl, coverImageUrl, slug, isActive)

    private fun Event.toEventResponse() = EventResponse(
        id, bar.id, title, date, time, maleCapacity, femaleCapacity,
        currentMaleCount, currentFemaleCount, price, status, description,
        choiceDeadline, matchNotificationTime, minAge, maxAge, maxChoices, matchingMode
    )
}
