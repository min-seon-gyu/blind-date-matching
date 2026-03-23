package com.blinddate.barowner.controller

import com.blinddate.auth.jwt.UserPrincipal
import com.blinddate.barowner.service.BarOwnerService
import com.blinddate.event.dto.EventCreateRequest
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/bar-owner/events")
class BarOwnerEventController(private val service: BarOwnerService) {

    @GetMapping
    fun getEvents(@AuthenticationPrincipal principal: UserPrincipal) = service.getMyEvents(principal.cafeId!!)

    @PostMapping
    fun createEvent(@AuthenticationPrincipal principal: UserPrincipal, @RequestBody request: EventCreateRequest) =
        service.createEvent(principal.cafeId!!, request)

    @PutMapping("/{id}")
    fun updateEvent(@AuthenticationPrincipal principal: UserPrincipal, @PathVariable id: Long, @RequestBody request: EventCreateRequest) =
        service.updateEvent(principal.cafeId!!, id, request)

    @DeleteMapping("/{id}")
    fun deleteEvent(@AuthenticationPrincipal principal: UserPrincipal, @PathVariable id: Long) =
        service.deleteEvent(principal.cafeId!!, id)

    @PutMapping("/{id}/close")
    fun closeEvent(@AuthenticationPrincipal principal: UserPrincipal, @PathVariable id: Long) =
        service.closeEvent(principal.cafeId!!, id)
}
