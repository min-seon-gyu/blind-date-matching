package com.blinddate.application.controller

import com.blinddate.application.service.ApplicationService
import com.blinddate.auth.jwt.UserPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
class ApplicationController(private val service: ApplicationService) {

    @PostMapping("/api/events/{eventId}/apply")
    fun apply(@AuthenticationPrincipal principal: UserPrincipal, @PathVariable eventId: Long) =
        service.apply(principal.id, eventId)

    @DeleteMapping("/api/events/{eventId}/apply")
    fun cancel(@AuthenticationPrincipal principal: UserPrincipal, @PathVariable eventId: Long) {
        val apps = service.getMyApplications(principal.id)
        val app = apps.find { it.eventId == eventId } ?: throw com.blinddate.common.exception.NotFoundException("신청 내역을 찾을 수 없습니다")
        service.cancel(principal.id, app.id)
    }

    @GetMapping("/api/me/applications")
    fun getMyApplications(@AuthenticationPrincipal principal: UserPrincipal) =
        service.getMyApplications(principal.id)
}
