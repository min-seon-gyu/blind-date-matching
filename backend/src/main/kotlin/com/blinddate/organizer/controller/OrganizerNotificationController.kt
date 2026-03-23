package com.blinddate.organizer.controller

import com.blinddate.auth.jwt.UserPrincipal
import com.blinddate.notification.entity.RecipientType
import com.blinddate.notification.service.NotificationService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/organizer/notifications")
class OrganizerNotificationController(private val notificationService: NotificationService) {
    @GetMapping
    fun getNotifications(@AuthenticationPrincipal p: UserPrincipal) =
        notificationService.getNotifications(RecipientType.ORGANIZER, p.id)

    @PutMapping("/{id}/read")
    fun markAsRead(@AuthenticationPrincipal p: UserPrincipal, @PathVariable id: Long) =
        notificationService.markAsRead(id, RecipientType.ORGANIZER, p.id)
}
