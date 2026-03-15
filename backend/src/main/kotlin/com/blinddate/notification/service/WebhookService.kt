package com.blinddate.notification.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class WebhookService(
    private val webClient: WebClient = WebClient.create()
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Value("\${app.webhook.url:}")
    private var webhookUrl: String = ""

    /**
     * POST JSON payload to the configured webhook URL (e.g., Slack incoming webhook).
     * If webhookUrl is empty, log and skip.
     */
    fun sendWebhook(url: String = webhookUrl, payload: Map<String, Any>) {
        val target = url.ifBlank { webhookUrl }
        if (target.isBlank()) {
            log.debug("[Webhook] No webhook URL configured, skipping. payload=$payload")
            return
        }

        try {
            webClient.post()
                .uri(target)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String::class.java)
                .doOnError { e -> log.warn("[Webhook] Failed to send webhook: ${e.message}") }
                .subscribe()
        } catch (e: Exception) {
            log.warn("[Webhook] Exception sending webhook: ${e.message}")
        }
    }
}
