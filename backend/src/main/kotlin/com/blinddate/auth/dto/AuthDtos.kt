package com.blinddate.auth.dto

data class KakaoLoginRequest(val code: String)
data class TokenResponse(val accessToken: String, val refreshToken: String, val isNewMember: Boolean)
data class KakaoUserInfo(val id: String, val email: String, val nickname: String)
