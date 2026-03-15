package com.blinddate.auth.service

import com.blinddate.auth.dto.KakaoUserInfo
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class KakaoOAuthService(
    @Value("\${kakao.client-id}") private val clientId: String,
    @Value("\${kakao.redirect-uri}") private val redirectUri: String
) {
    private val webClient = WebClient.create()

    fun getAccessToken(code: String): String {
        val response = webClient.post()
            .uri("https://kauth.kakao.com/oauth/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue("grant_type=authorization_code&client_id=$clientId&redirect_uri=$redirectUri&code=$code")
            .retrieve()
            .bodyToMono(Map::class.java)
            .block()!!
        return response["access_token"] as String
    }

    fun getUserInfo(accessToken: String): KakaoUserInfo {
        val response = webClient.get()
            .uri("https://kapi.kakao.com/v2/user/me")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .bodyToMono(Map::class.java)
            .block()!!
        val id = response["id"].toString()
        val kakaoAccount = response["kakao_account"] as? Map<*, *>
        val profile = kakaoAccount?.get("profile") as? Map<*, *>
        return KakaoUserInfo(
            id = id,
            email = kakaoAccount?.get("email") as? String ?: "",
            nickname = profile?.get("nickname") as? String ?: ""
        )
    }
}
