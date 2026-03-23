package com.blinddate.bar.controller

import com.blinddate.bar.service.BarService
import com.blinddate.event.service.EventService
import org.springframework.web.bind.annotation.*

/**
 * Deprecated: replaced by CafeController.
 * Kept for backward compatibility.
 */
@RestController
@RequestMapping("/api/bars")
class BarController(private val barService: BarService, private val eventService: EventService) {
    @GetMapping("/{slug}")
    fun getBar(@PathVariable slug: String) = barService.getBySlug(slug)

    @GetMapping("/{slug}/events")
    fun getEvents(@PathVariable slug: String) = eventService.getEventsByCafe(slug)

    @GetMapping("/{slug}/events/{eventId}")
    fun getEvent(@PathVariable slug: String, @PathVariable eventId: Long) =
        eventService.getEventByCafeSlugAndId(slug, eventId)
}
