package com.blinddate.notification.service

import com.blinddate.common.entity.BaseEntity
import com.blinddate.common.exception.ForbiddenException
import com.blinddate.member.entity.Member
import com.blinddate.member.repository.MemberRepository
import com.blinddate.notification.entity.Notification
import com.blinddate.notification.entity.NotificationType
import com.blinddate.notification.repository.NotificationRepository
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.redis.core.ListOperations
import org.springframework.data.redis.core.RedisTemplate
import java.util.Optional

class NotificationServiceTest {

    private lateinit var notificationService: NotificationService
    private val notificationRepository = mockk<NotificationRepository>()
    private val memberRepository = mockk<MemberRepository>()
    private val redisTemplate = mockk<RedisTemplate<String, String>>()
    private val objectMapper = ObjectMapper()

    @BeforeEach
    fun setUp() {
        notificationService = NotificationService(
            notificationRepository,
            memberRepository,
            redisTemplate,
            objectMapper
        )
    }

    private fun <T : BaseEntity> T.setId(id: Long): T {
        val f = BaseEntity::class.java.getDeclaredField("id")
        f.isAccessible = true
        f.set(this, id)
        return this
    }

    private fun createMember(id: Long): Member =
        Member(kakaoId = "kakao$id", nickname = "user$id", phoneNumber = "010-0000-000$id").setId(id)

    private fun createNotification(id: Long, member: Member, isRead: Boolean = false): Notification =
        Notification(
            member = member,
            type = NotificationType.APPROVED,
            title = "승인 완료",
            message = "이벤트 참가가 승인되었습니다.",
            isRead = isRead
        ).setId(id)

    @Test
    fun `should send notification and save to DB`() {
        val member = createMember(1L)
        val notification = createNotification(1L, member)

        every { memberRepository.findById(1L) } returns Optional.of(member)
        every { notificationRepository.save(any()) } returns notification

        val listOps = mockk<ListOperations<String, String>>()
        every { redisTemplate.opsForList() } returns listOps
        every { listOps.leftPush(any(), any()) } returns 1L

        notificationService.send(1L, NotificationType.APPROVED, "승인 완료", "이벤트 참가가 승인되었습니다.")

        verify { notificationRepository.save(any()) }
        verify { listOps.leftPush(any(), any()) }
    }

    @Test
    fun `should mark notification as read`() {
        val member = createMember(1L)
        val notification = createNotification(1L, member, isRead = false)

        every { notificationRepository.findById(1L) } returns Optional.of(notification)

        notificationService.markAsRead(1L, 1L)

        assertTrue(notification.isRead)
    }

    @Test
    fun `should throw when marking another member notification as read`() {
        val member = createMember(1L)
        val notification = createNotification(1L, member)

        every { notificationRepository.findById(1L) } returns Optional.of(notification)

        assertThrows<ForbiddenException> {
            notificationService.markAsRead(1L, 999L)
        }
    }

    @Test
    fun `should count unread notifications`() {
        every { notificationRepository.countByMemberIdAndIsReadFalse(1L) } returns 3L

        val result = notificationService.getUnreadCount(1L)

        assertEquals(3L, result.count)
    }
}
