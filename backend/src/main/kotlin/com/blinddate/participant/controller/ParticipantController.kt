package com.blinddate.participant.controller

import com.blinddate.auth.jwt.UserPrincipal
import com.blinddate.participant.dto.ProfileCreateRequest
import com.blinddate.participant.service.ParticipantService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/me")
class ParticipantController(private val service: ParticipantService) {

    @GetMapping
    fun getMe(@AuthenticationPrincipal principal: UserPrincipal) = service.getMe(principal.id)

    @GetMapping("/profile")
    fun getProfile(@AuthenticationPrincipal principal: UserPrincipal) = service.getProfile(principal.id)

    @PostMapping("/profile")
    fun createProfile(@AuthenticationPrincipal principal: UserPrincipal, @RequestBody request: ProfileCreateRequest) =
        service.createProfile(principal.id, request)

    @PutMapping("/profile")
    fun updateProfile(@AuthenticationPrincipal principal: UserPrincipal, @RequestBody request: ProfileCreateRequest) =
        service.updateProfile(principal.id, request)
}
