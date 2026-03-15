package com.blinddate.notification.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class NotificationQueueConsumer(
    private val redisTemplate: RedisTemplate<String, String>,
    private val kakaoAlimtalkService: KakaoAlimtalkService,
    private val webhookService: WebhookService,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val QUEUE_KEY = "notification:queue"
        const val DLQ_KEY = "notification:dlq"
        const val MAX_RETRIES = 3
    }

    @Scheduled(fixedDelay = 1000)
    fun consume() {
        try {
            val result = redisTemplate.opsForList().rightPop(QUEUE_KEY, 1, TimeUnit.SECONDS)
            result?.let { message ->
                processWithRetry(message, 0)
            }
        } catch (e: Exception) {
            log.warn("[NotificationConsumer] Redis unavailable or error polling queue: ${e.message}")
        }
    }

    private fun processWithRetry(message: String, attempt: Int) {
        try {
            val payload = objectMapper.readValue(message, Map::class.java)
            dispatch(payload)
        } catch (e: Exception) {
            log.warn("[NotificationConsumer] Processing failed (attempt ${attempt + 1}/$MAX_RETRIES): ${e.message}")
            if (attempt + 1 < MAX_RETRIES) {
                val delayMs = Math.pow(4.0, attempt.toDouble()).toLong() * 1000L // 1s, 4s, 16s
                Thread.sleep(delayMs)
                processWithRetry(message, attempt + 1)
            } else {
                log.error("[NotificationConsumer] Max retries reached, pushing to DLQ: $message")
                try {
                    redisTemplate.opsForList().leftPush(DLQ_KEY, message)
                } catch (dlqEx: Exception) {
                    log.error("[NotificationConsumer] Failed to push to DLQ: ${dlqEx.message}")
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun dispatch(payload: Map<*, *>) {
        val type = payload["type"]?.toString() ?: return
        val phoneNumber = payload["phoneNumber"]?.toString() ?: ""
        val title = payload["title"]?.toString() ?: ""
        val message = payload["message"]?.toString() ?: ""

        log.info("[NotificationConsumer] Dispatching notification type=$type")

        // Send Kakao Alimtalk for all notification types when phone number is available
        if (phoneNumber.isNotBlank()) {
            kakaoAlimtalkService.sendAlimtalk(
                phoneNumber = phoneNumber,
                templateCode = type,
                variables = mapOf("title" to title, "message" to message)
            )
        }

        // Send webhook for MATCH_RESULT and APPROVED
        if (type in listOf("MATCH_RESULT", "APPROVED")) {
            webhookService.sendWebhook(
                payload = mapOf("type" to type, "title" to title, "message" to message)
            )
        }
    }
}
