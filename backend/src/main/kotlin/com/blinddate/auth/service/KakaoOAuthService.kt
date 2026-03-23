package com.blinddate.auth.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class KakaoOAuthService(
    @Value("\${kakao.client-id}") private val clientId: String,
    @Value("\${kakao.redirect-uri}") private val redirectUri: String
) {
    private val webClient = WebClient.create()

    data class KakaoTokenResponse(val access_token: String)
    data class KakaoUserResponse(val id: Long, val kakao_account: KakaoAccount?)
    data class KakaoAccount(val email: String?, val profile: KakaoProfile?)
    data class KakaoProfile(val nickname: String?)

    fun getAccessToken(code: String): String {
        val response = webClient.post()
            .uri("https://kauth.kakao.com/oauth/token")
            .bodyValue("grant_type=authorization_code&client_id=$clientId&redirect_uri=$redirectUri&code=$code")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .retrieve()
            .bodyToMono(KakaoTokenResponse::class.java)
            .block()!!
        return response.access_token
    }

    fun getUserInfo(accessToken: String): KakaoUserResponse {
        return webClient.get()
            .uri("https://kapi.kakao.com/v2/user/me")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .bodyToMono(KakaoUserResponse::class.java)
            .block()!!
    }
}
