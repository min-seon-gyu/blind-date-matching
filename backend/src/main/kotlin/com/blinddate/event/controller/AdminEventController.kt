package com.blinddate.event.controller

import com.blinddate.event.dto.*
import com.blinddate.event.service.EventService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/events")
class AdminEventController(private val eventService: EventService) {
    private fun currentMemberId(): Long = SecurityContextHolder.getContext().authentication.principal as Long

    @PostMapping
    fun create(@Valid @RequestBody request: EventCreateRequest): ResponseEntity<EventResponse> =
        ResponseEntity.ok(eventService.createEvent(currentMemberId(), request))

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: EventCreateRequest): ResponseEntity<EventResponse> =
        ResponseEntity.ok(eventService.updateEvent(id, request))

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        eventService.deleteEvent(id); return ResponseEntity.noContent().build()
    }
}
