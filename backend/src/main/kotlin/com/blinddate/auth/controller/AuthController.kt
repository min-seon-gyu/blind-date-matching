package com.blinddate.auth.controller

import com.blinddate.auth.dto.KakaoLoginRequest
import com.blinddate.auth.dto.TokenResponse
import com.blinddate.auth.service.AuthService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/kakao/login")
    fun kakaoLogin(@RequestBody request: KakaoLoginRequest): ResponseEntity<TokenResponse> =
        ResponseEntity.ok(authService.kakaoLogin(request.code))

    @PostMapping("/refresh")
    fun refresh(@RequestBody body: Map<String, String>): ResponseEntity<TokenResponse> {
        val refreshToken = body["refreshToken"] ?: return ResponseEntity.badRequest().build()
        return ResponseEntity.ok(authService.refresh(refreshToken))
    }

    @DeleteMapping("/logout")
    fun logout(request: HttpServletRequest): ResponseEntity<Void> {
        val memberId = SecurityContextHolder.getContext().authentication.principal as Long
        val token = request.getHeader("Authorization")?.substring(7) ?: ""
        authService.logout(memberId, token)
        return ResponseEntity.noContent().build()
    }
}
