package com.blinddate.action.service

import com.blinddate.action.entity.ActionToken
import com.blinddate.action.repository.ActionTokenRepository
import com.blinddate.common.exception.BadRequestException
import com.blinddate.common.exception.NotFoundException
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.Optional

class ActionTokenServiceTest {
    private val repo = mockk<ActionTokenRepository>()
    private val service = ActionTokenService(repo)

    @Test
    fun `createToken should save and return token string`() {
        every { repo.save(any()) } answers { firstArg() }
        val token = service.createToken("APPROVE_APPLICATION", 1L, 1L)
        assertNotNull(token)
        verify { repo.save(any()) }
    }

    @Test
    fun `getActionInfo should return info for valid token`() {
        val actionToken = ActionToken(actionType = "APPROVE_APPLICATION", targetId = 1L, barOwnerId = 1L)
        every { repo.findByToken(actionToken.token) } returns Optional.of(actionToken)
        val result = service.getActionInfo(actionToken.token)
        assertEquals("APPROVE_APPLICATION", result.actionType)
        assertFalse(result.expired)
        assertFalse(result.used)
    }

    @Test
    fun `getActionInfo should throw for unknown token`() {
        every { repo.findByToken("bad") } returns Optional.empty()
        assertThrows(NotFoundException::class.java) { service.getActionInfo("bad") }
    }

    @Test
    fun `executeAction should mark as used`() {
        val actionToken = ActionToken(actionType = "APPROVE_APPLICATION", targetId = 1L, barOwnerId = 1L)
        every { repo.findByToken(actionToken.token) } returns Optional.of(actionToken)
        val result = service.executeAction(actionToken.token)
        assertTrue(actionToken.used)
        assertTrue(result.success)
    }

    @Test
    fun `executeAction should reject expired token`() {
        val actionToken = ActionToken(
            actionType = "APPROVE_APPLICATION", targetId = 1L, barOwnerId = 1L,
            expiresAt = LocalDateTime.now().minusHours(1)
        )
        every { repo.findByToken(actionToken.token) } returns Optional.of(actionToken)
        assertThrows(BadRequestException::class.java) { service.executeAction(actionToken.token) }
    }

    @Test
    fun `executeAction should reject used token`() {
        val actionToken = ActionToken(actionType = "APPROVE_APPLICATION", targetId = 1L, barOwnerId = 1L, used = true)
        every { repo.findByToken(actionToken.token) } returns Optional.of(actionToken)
        assertThrows(BadRequestException::class.java) { service.executeAction(actionToken.token) }
    }
}
