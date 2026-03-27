package com.blinddate.cafeowner.controller

import com.blinddate.auth.dto.EmailLoginRequest
import com.blinddate.auth.service.CafeOwnerAuthService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/cafe-owner/auth")
class CafeOwnerAuthController(private val cafeOwnerAuthService: CafeOwnerAuthService) {

    @PostMapping("/login")
    fun login(@RequestBody request: EmailLoginRequest) = cafeOwnerAuthService.login(request.email, request.password)
}
