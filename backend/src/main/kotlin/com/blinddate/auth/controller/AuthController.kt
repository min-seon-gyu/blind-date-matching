package com.blinddate.auth.controller

import com.blinddate.auth.dto.*
import com.blinddate.auth.service.ParticipantAuthService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(private val participantAuthService: ParticipantAuthService) {

    @PostMapping("/kakao")
    fun kakaoLogin(@RequestBody request: KakaoLoginRequest) = participantAuthService.kakaoLogin(request.code)

    @PostMapping("/refresh")
    fun refresh(@RequestBody request: RefreshRequest) = participantAuthService.refreshToken(request.refreshToken)
}
