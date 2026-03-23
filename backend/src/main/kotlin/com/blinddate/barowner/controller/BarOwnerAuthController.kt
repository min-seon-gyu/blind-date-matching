package com.blinddate.barowner.controller

import com.blinddate.auth.dto.EmailLoginRequest
import com.blinddate.auth.service.BarOwnerAuthService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/bar-owner/auth")
class BarOwnerAuthController(private val barOwnerAuthService: BarOwnerAuthService) {

    @PostMapping("/login")
    fun login(@RequestBody request: EmailLoginRequest) = barOwnerAuthService.login(request.email, request.password)
}
