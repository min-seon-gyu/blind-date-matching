package com.blinddate.cafeowner.controller

import com.blinddate.auth.jwt.UserPrincipal
import com.blinddate.event.service.EventService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/cafe-owner/events")
class CafeOwnerEventController(private val eventService: EventService) {
    @GetMapping
    fun getEvents(@AuthenticationPrincipal p: UserPrincipal) = eventService.getByCafeId(p.cafeId!!)
}
