package com.blinddate.notification.controller

import com.blinddate.auth.jwt.UserPrincipal
import com.blinddate.notification.entity.RecipientType
import com.blinddate.notification.service.NotificationService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/me/notifications")
class NotificationController(private val service: NotificationService) {

    @GetMapping
    fun getNotifications(@AuthenticationPrincipal principal: UserPrincipal) =
        service.getNotifications(RecipientType.PARTICIPANT, principal.id)

    @GetMapping("/unread-count")
    fun getUnreadCount(@AuthenticationPrincipal principal: UserPrincipal) =
        service.getUnreadCount(RecipientType.PARTICIPANT, principal.id)

    @PutMapping("/{id}/read")
    fun markAsRead(@AuthenticationPrincipal principal: UserPrincipal, @PathVariable id: Long) =
        service.markAsRead(id, RecipientType.PARTICIPANT, principal.id)
}
