package com.blinddate.notification.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class KakaoAlimtalkService {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Placeholder implementation — logs the call.
     * Real implementation requires Kakao 비즈메시지 API credentials.
     */
    fun sendAlimtalk(phoneNumber: String, templateCode: String, variables: Map<String, String>) {
        log.info(
            "[KakaoAlimtalk] Sending alimtalk to=$phoneNumber, templateCode=$templateCode, variables=$variables"
        )
        // TODO: Replace with actual Kakao 비즈메시지 API call once credentials are available
        //
        // Example (pseudocode):
        // webClient.post()
        //     .uri("https://kakaoapi.aligo.in/akv10/alimtalk/send/")
        //     .bodyValue(buildRequest(phoneNumber, templateCode, variables))
        //     .retrieve()
        //     .bodyToMono(String::class.java)
        //     .block()
    }
}
