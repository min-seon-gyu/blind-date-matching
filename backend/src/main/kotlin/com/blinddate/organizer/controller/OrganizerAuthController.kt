package com.blinddate.organizer.controller

import com.blinddate.auth.dto.EmailLoginRequest
import com.blinddate.auth.service.OrganizerAuthService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/organizer/auth")
class OrganizerAuthController(private val organizerAuthService: OrganizerAuthService) {

    @PostMapping("/login")
    fun login(@RequestBody request: EmailLoginRequest) = organizerAuthService.login(request.email, request.password)
}
