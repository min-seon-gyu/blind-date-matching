package com.blinddate.admin.controller

import com.blinddate.auth.dto.EmailLoginRequest
import com.blinddate.auth.service.AdminAuthService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/auth")
class AdminAuthController(private val adminAuthService: AdminAuthService) {

    @PostMapping("/login")
    fun login(@RequestBody request: EmailLoginRequest) = adminAuthService.login(request.email, request.password)
}
