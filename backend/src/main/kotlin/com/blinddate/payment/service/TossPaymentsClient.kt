package com.blinddate.payment.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.util.Base64

@Component
class TossPaymentsClient(
    @Value("\${toss.secret-key}") private val secretKey: String,
    @Value("\${toss.base-url}") private val baseUrl: String
) {
    private val webClient: WebClient by lazy {
        WebClient.builder()
            .baseUrl(baseUrl)
            .build()
    }

    private fun basicAuthHeader(): String {
        val credentials = "$secretKey:"
        return "Basic " + Base64.getEncoder().encodeToString(credentials.toByteArray())
    }

    fun confirmPayment(paymentKey: String, orderId: String, amount: Int): Map<*, *> {
        return webClient.post()
            .uri("/v1/payments/confirm")
            .header("Authorization", basicAuthHeader())
            .header("Content-Type", "application/json")
            .bodyValue(mapOf("paymentKey" to paymentKey, "orderId" to orderId, "amount" to amount))
            .retrieve()
            .bodyToMono(Map::class.java)
            .block() ?: emptyMap<String, Any>()
    }

    fun cancelPayment(paymentKey: String, cancelReason: String): Map<*, *> {
        return webClient.post()
            .uri("/v1/payments/$paymentKey/cancel")
            .header("Authorization", basicAuthHeader())
            .header("Content-Type", "application/json")
            .bodyValue(mapOf("cancelReason" to cancelReason))
            .retrieve()
            .bodyToMono(Map::class.java)
            .block() ?: emptyMap<String, Any>()
    }
}
