package com.blinddate.organizer.controller

import com.blinddate.application.dto.RejectRequest
import com.blinddate.application.service.ApplicationService
import com.blinddate.auth.jwt.UserPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/organizer")
class OrganizerApplicationController(private val service: ApplicationService) {

    @GetMapping("/events/{eventId}/applications")
    fun getApplications(@AuthenticationPrincipal principal: UserPrincipal, @PathVariable eventId: Long) =
        service.getByEventForOrganizer(principal.id, eventId)

    @PutMapping("/applications/{id}/approve")
    fun approve(@AuthenticationPrincipal principal: UserPrincipal, @PathVariable id: Long) =
        service.approve(principal.id, id)

    @PutMapping("/applications/{id}/reject")
    fun reject(@AuthenticationPrincipal principal: UserPrincipal, @PathVariable id: Long, @RequestBody request: RejectRequest) =
        service.reject(principal.id, id, request.reason)
}
