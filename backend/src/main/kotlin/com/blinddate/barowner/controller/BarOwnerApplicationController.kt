package com.blinddate.barowner.controller

import com.blinddate.application.dto.RejectRequest
import com.blinddate.application.service.ApplicationService
import com.blinddate.auth.jwt.UserPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/bar-owner")
class BarOwnerApplicationController(private val service: ApplicationService) {

    @GetMapping("/events/{eventId}/applications")
    fun getApplications(@AuthenticationPrincipal principal: UserPrincipal, @PathVariable eventId: Long) =
        service.getApplicationsByEvent(principal.cafeId!!, eventId)

    @PutMapping("/applications/{id}/approve")
    fun approve(@AuthenticationPrincipal principal: UserPrincipal, @PathVariable id: Long) =
        service.approve(principal.id, principal.cafeId!!, id)

    @PutMapping("/applications/{id}/reject")
    fun reject(@AuthenticationPrincipal principal: UserPrincipal, @PathVariable id: Long, @RequestBody request: RejectRequest) =
        service.reject(principal.id, principal.cafeId!!, id, request.reason)
}
