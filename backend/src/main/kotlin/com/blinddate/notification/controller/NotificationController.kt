package com.blinddate.notification.controller

import com.blinddate.notification.dto.NotificationResponse
import com.blinddate.notification.dto.UnreadCountResponse
import com.blinddate.notification.service.NotificationService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val notificationService: NotificationService
) {

    @GetMapping
    fun getNotifications(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<List<NotificationResponse>> {
        val memberId = userDetails.username.toLong()
        return ResponseEntity.ok(notificationService.getNotifications(memberId))
    }

    @PutMapping("/{id}/read")
    fun markAsRead(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Void> {
        val memberId = userDetails.username.toLong()
        notificationService.markAsRead(id, memberId)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/unread-count")
    fun getUnreadCount(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<UnreadCountResponse> {
        val memberId = userDetails.username.toLong()
        return ResponseEntity.ok(notificationService.getUnreadCount(memberId))
    }
}
