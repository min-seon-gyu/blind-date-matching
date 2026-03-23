package com.blinddate.notification.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class KakaoChannelMessageService(
    @Value("\${kakao.channel.admin-key:}") private val adminKey: String,
    @Value("\${kakao.channel.sender-key:}") private val senderKey: String
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val webClient = WebClient.create()

    fun sendMessage(recipientKakaoId: String, title: String, message: String, buttonUrl: String? = null) {
        if (adminKey.isBlank()) {
            log.debug("[KakaoChannel] Admin key not configured, skipping message send")
            return
        }

        try {
            val templateObject = buildTemplateObject(title, message, buttonUrl)

            webClient.post()
                .uri("https://kapi.kakao.com/v1/api/talk/friends/message/default/send")
                .header("Authorization", "KakaoAK $adminKey")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue("receiver_uuids=[\"$recipientKakaoId\"]&template_object=$templateObject")
                .retrieve()
                .bodyToMono(String::class.java)
                .subscribe(
                    { log.info("[KakaoChannel] Message sent to $recipientKakaoId") },
                    { e -> log.warn("[KakaoChannel] Failed to send message: ${e.message}") }
                )
        } catch (e: Exception) {
            log.warn("[KakaoChannel] Error sending message: ${e.message}")
        }
    }

    private fun buildTemplateObject(title: String, message: String, buttonUrl: String?): String {
        val button = if (buttonUrl != null) {
            ""","buttons":[{"title":"확인하기","link":{"web_url":"$buttonUrl","mobile_web_url":"$buttonUrl"}}]"""
        } else ""

        return """{"object_type":"text","text":"[$title] $message","link":{"web_url":"https://example.com","mobile_web_url":"https://example.com"}$button}"""
    }
}
