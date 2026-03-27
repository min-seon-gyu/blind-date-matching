package com.blinddate.organizer.service

import com.blinddate.cafe.repository.CafeRepository
import com.blinddate.common.exception.BadRequestException
import com.blinddate.common.exception.ForbiddenException
import com.blinddate.common.exception.NotFoundException
import com.blinddate.event.dto.EventResponse
import com.blinddate.event.entity.Event
import com.blinddate.event.entity.EventStatus
import com.blinddate.event.repository.EventRepository
import com.blinddate.organizer.dto.CreateEventRequest
import com.blinddate.organizer.dto.UpdateEventRequest
import com.blinddate.organizer.repository.OrganizerRepository
import com.blinddate.partnership.service.PartnershipService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class OrganizerService(
    private val organizerRepository: OrganizerRepository,
    private val cafeRepository: CafeRepository,
    private val eventRepository: EventRepository,
    private val partnershipService: PartnershipService
) {
    @Transactional
    fun createEvent(organizerId: Long, request: CreateEventRequest): EventResponse {
        if (!partnershipService.hasActivePartnership(request.cafeId, organizerId)) {
            throw BadRequestException("먼저 카페와 제휴를 맺어주세요")
        }

        val organizer = organizerRepository.findById(organizerId)
            .orElseThrow { NotFoundException("주최자를 찾을 수 없습니다") }
        val cafe = cafeRepository.findById(request.cafeId)
            .orElseThrow { NotFoundException("카페를 찾을 수 없습니다") }

        val event = eventRepository.save(Event(
            cafe = cafe, organizer = organizer, title = request.title, date = request.date,
            time = request.time, price = request.price, maleCapacity = request.maleCapacity,
            femaleCapacity = request.femaleCapacity, description = request.description,
            choiceDeadline = request.choiceDeadline, matchNotificationTime = request.matchNotificationTime,
            minAge = request.minAge, maxAge = request.maxAge, maxChoices = request.maxChoices,
            matchingMode = request.matchingMode
        ))
        return event.toEventResponse()
    }

    fun getMyEvents(organizerId: Long): List<EventResponse> {
        return eventRepository.findByOrganizerIdAndDeletedAtIsNull(organizerId).map { it.toEventResponse() }
    }

    @Transactional
    fun updateEvent(organizerId: Long, eventId: Long, request: UpdateEventRequest): EventResponse {
        val event = getOwnedEvent(organizerId, eventId)
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
    fun deleteEvent(organizerId: Long, eventId: Long) {
        val event = getOwnedEvent(organizerId, eventId)
        event.deletedAt = LocalDateTime.now()
    }

    @Transactional
    fun closeEvent(organizerId: Long, eventId: Long): EventResponse {
        val event = getOwnedEvent(organizerId, eventId)
        if (event.status != EventStatus.OPEN) throw BadRequestException("OPEN 상태인 이벤트만 마감할 수 있습니다")
        event.status = EventStatus.CLOSED
        return event.toEventResponse()
    }

    private fun getOwnedEvent(organizerId: Long, eventId: Long): Event {
        val event = eventRepository.findById(eventId).orElseThrow { NotFoundException("이벤트를 찾을 수 없습니다") }
        if (event.isDeleted()) throw NotFoundException("삭제된 이벤트입니다")
        if (event.organizer.id != organizerId) throw ForbiddenException("해당 이벤트에 대한 권한이 없습니다")
        return event
    }

    private fun Event.toEventResponse() = EventResponse(
        id, cafe.id, organizer.id, title, date, time, maleCapacity, femaleCapacity,
        currentMaleCount, currentFemaleCount, price, status, description,
        choiceDeadline, matchNotificationTime, minAge, maxAge, maxChoices, matchingMode
    )
}
