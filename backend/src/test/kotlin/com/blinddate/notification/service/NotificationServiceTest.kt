package com.blinddate.notification.service

import com.blinddate.common.exception.ForbiddenException
import com.blinddate.common.exception.NotFoundException
import com.blinddate.notification.entity.Notification
import com.blinddate.notification.entity.NotificationType
import com.blinddate.notification.entity.RecipientType
import com.blinddate.notification.repository.NotificationRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.Optional

class NotificationServiceTest {
    private val repo = mockk<NotificationRepository>()
    private val service = NotificationService(repo)

    @Test
    fun `send should save notification`() {
        every { repo.save(any()) } answers { firstArg() }
        val result = service.send(RecipientType.PARTICIPANT, 1L, NotificationType.APPROVED, "승인", "승인되었습니다")
        assertEquals("승인", result.title)
        verify { repo.save(any()) }
    }

    @Test
    fun `getNotifications should return for participant`() {
        val n = Notification(RecipientType.PARTICIPANT, 1L, NotificationType.APPROVED, "Title", "Message")
        every { repo.findByRecipientTypeAndRecipientIdOrderByCreatedAtDesc(RecipientType.PARTICIPANT, 1L) } returns listOf(n)
        val result = service.getNotifications(RecipientType.PARTICIPANT, 1L)
        assertEquals(1, result.size)
    }

    @Test
    fun `markAsRead should update isRead`() {
        val n = Notification(RecipientType.PARTICIPANT, 1L, NotificationType.APPROVED, "Title", "Message")
        every { repo.findById(1L) } returns Optional.of(n)
        service.markAsRead(1L, RecipientType.PARTICIPANT, 1L)
        assertTrue(n.isRead)
    }

    @Test
    fun `markAsRead should throw for wrong recipient`() {
        val n = Notification(RecipientType.PARTICIPANT, 1L, NotificationType.APPROVED, "Title", "Message")
        every { repo.findById(1L) } returns Optional.of(n)
        assertThrows(ForbiddenException::class.java) {
            service.markAsRead(1L, RecipientType.PARTICIPANT, 999L)
        }
    }

    @Test
    fun `getUnreadCount should return count`() {
        every { repo.countByRecipientTypeAndRecipientIdAndIsReadFalse(RecipientType.PARTICIPANT, 1L) } returns 5L
        val result = service.getUnreadCount(RecipientType.PARTICIPANT, 1L)
        assertEquals(5L, result.count)
    }
}
