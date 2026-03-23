package com.blinddate.auth.dto

data class KakaoLoginRequest(val code: String)
data class EmailLoginRequest(val email: String, val password: String)
data class TokenResponse(val accessToken: String, val refreshToken: String, val isNewUser: Boolean = false)
data class RefreshRequest(val refreshToken: String)
