package com.blinddate.event.controller

import com.blinddate.event.dto.EventResponse
import com.blinddate.event.service.EventService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/events")
class EventController(private val eventService: EventService) {
    @GetMapping
    fun getEvents(@RequestParam year: Int, @RequestParam month: Int): ResponseEntity<List<EventResponse>> =
        ResponseEntity.ok(eventService.getEventsByMonth(year, month))

    @GetMapping("/{id}")
    fun getEvent(@PathVariable id: Long): ResponseEntity<EventResponse> =
        ResponseEntity.ok(eventService.getEvent(id))
}
