package com.blinddate.cafe.controller

import com.blinddate.cafe.service.CafeService
import com.blinddate.event.service.EventService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/cafes")
class CafeController(
    private val cafeService: CafeService,
    private val eventService: EventService
) {
    @GetMapping("/{slug}")
    fun getCafe(@PathVariable slug: String) = cafeService.getBySlug(slug)

    @GetMapping("/{slug}/events")
    fun getEvents(@PathVariable slug: String) = eventService.getEventsByCafe(slug)

    @GetMapping("/{slug}/events/{eventId}")
    fun getEvent(@PathVariable slug: String, @PathVariable eventId: Long) =
        eventService.getEventByCafeSlugAndId(slug, eventId)
}
