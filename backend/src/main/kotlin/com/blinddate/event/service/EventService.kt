package com.blinddate.event.service

import com.blinddate.common.exception.NotFoundException
import com.blinddate.event.dto.*
import com.blinddate.event.entity.BlindDateEvent
import com.blinddate.event.repository.BlindDateEventRepository
import com.blinddate.member.repository.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class EventService(
    private val eventRepository: BlindDateEventRepository,
    private val memberRepository: MemberRepository
) {
    fun getEventsByMonth(year: Int, month: Int): List<EventResponse> =
        eventRepository.findByYearAndMonth(year, month).map { it.toResponse() }

    fun getEvent(eventId: Long): EventResponse {
        val event = eventRepository.findById(eventId).orElseThrow { NotFoundException("이벤트를 찾을 수 없습니다") }
        if (event.isDeleted()) throw NotFoundException("삭제된 이벤트입니다")
        return event.toResponse()
    }

    @Transactional
    fun createEvent(adminId: Long, request: EventCreateRequest): EventResponse {
        val admin = memberRepository.findById(adminId).orElseThrow { NotFoundException("관리자를 찾을 수 없습니다") }
        return eventRepository.save(BlindDateEvent(
            title = request.title, date = request.date, time = request.time,
            maleCapacity = request.maleCapacity, femaleCapacity = request.femaleCapacity,
            price = request.price, description = request.description,
            choiceDeadline = request.choiceDeadline, matchNotificationTime = request.matchNotificationTime,
            minAge = request.minAge, maxAge = request.maxAge, createdBy = admin
        )).toResponse()
    }

    @Transactional
    fun updateEvent(eventId: Long, request: EventCreateRequest): EventResponse {
        val event = eventRepository.findById(eventId).orElseThrow { NotFoundException("이벤트를 찾을 수 없습니다") }
        event.apply {
            title = request.title; date = request.date; time = request.time
            maleCapacity = request.maleCapacity; femaleCapacity = request.femaleCapacity
            price = request.price; description = request.description
            choiceDeadline = request.choiceDeadline; matchNotificationTime = request.matchNotificationTime
            minAge = request.minAge; maxAge = request.maxAge
        }
        return event.toResponse()
    }

    @Transactional
    fun deleteEvent(eventId: Long) {
        val event = eventRepository.findById(eventId).orElseThrow { NotFoundException("이벤트를 찾을 수 없습니다") }
        event.deletedAt = LocalDateTime.now()
    }

    private fun BlindDateEvent.toResponse() = EventResponse(
        id, title, date, time, maleCapacity, femaleCapacity,
        currentMaleCount, currentFemaleCount, price, status, description,
        choiceDeadline, matchNotificationTime, minAge, maxAge
    )
}
