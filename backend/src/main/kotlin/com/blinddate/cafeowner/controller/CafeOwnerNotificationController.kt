package com.blinddate.cafeowner.controller

import com.blinddate.auth.jwt.UserPrincipal
import com.blinddate.notification.entity.RecipientType
import com.blinddate.notification.service.NotificationService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/cafe-owner/notifications")
class CafeOwnerNotificationController(private val notificationService: NotificationService) {
    @GetMapping
    fun getNotifications(@AuthenticationPrincipal p: UserPrincipal) =
        notificationService.getNotifications(RecipientType.CAFE_OWNER, p.id)

    @PutMapping("/{id}/read")
    fun markAsRead(@AuthenticationPrincipal p: UserPrincipal, @PathVariable id: Long) =
        notificationService.markAsRead(id, RecipientType.CAFE_OWNER, p.id)
}
