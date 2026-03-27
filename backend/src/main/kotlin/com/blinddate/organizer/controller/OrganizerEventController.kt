package com.blinddate.organizer.controller

import com.blinddate.auth.jwt.UserPrincipal
import com.blinddate.organizer.dto.CreateEventRequest
import com.blinddate.organizer.dto.UpdateEventRequest
import com.blinddate.organizer.service.OrganizerService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/organizer/events")
class OrganizerEventController(private val service: OrganizerService) {

    @GetMapping
    fun getEvents(@AuthenticationPrincipal principal: UserPrincipal) = service.getMyEvents(principal.id)

    @PostMapping
    fun createEvent(@AuthenticationPrincipal principal: UserPrincipal, @RequestBody request: CreateEventRequest) =
        service.createEvent(principal.id, request)

    @PutMapping("/{id}")
    fun updateEvent(@AuthenticationPrincipal principal: UserPrincipal, @PathVariable id: Long, @RequestBody request: UpdateEventRequest) =
        service.updateEvent(principal.id, id, request)

    @DeleteMapping("/{id}")
    fun deleteEvent(@AuthenticationPrincipal principal: UserPrincipal, @PathVariable id: Long) =
        service.deleteEvent(principal.id, id)

    @PutMapping("/{id}/close")
    fun closeEvent(@AuthenticationPrincipal principal: UserPrincipal, @PathVariable id: Long) =
        service.closeEvent(principal.id, id)
}
