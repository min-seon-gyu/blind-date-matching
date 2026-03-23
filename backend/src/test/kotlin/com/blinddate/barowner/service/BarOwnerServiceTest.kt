package com.blinddate.barowner.service

import com.blinddate.bar.entity.Bar
import com.blinddate.bar.repository.BarRepository
import com.blinddate.barowner.dto.BarUpdateRequest
import com.blinddate.common.exception.NotFoundException
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.Optional

class BarOwnerServiceTest {
    private val barRepo = mockk<BarRepository>()
    private val service = BarOwnerService(barRepo)

    private val bar = Bar(name = "Test Bar", address = "addr", slug = "test-bar")

    @Test
    fun `getMyBar should return bar info`() {
        every { barRepo.findById(bar.id) } returns Optional.of(bar)
        val result = service.getMyBar(bar.id)
        assertEquals("Test Bar", result.name)
    }

    @Test
    fun `updateMyBar should update fields`() {
        every { barRepo.findById(bar.id) } returns Optional.of(bar)
        val request = BarUpdateRequest(name = "New Name", address = "new addr", description = "desc")
        val result = service.updateMyBar(bar.id, request)
        assertEquals("New Name", result.name)
    }

    @Test
    fun `getMyBar should throw for unknown bar`() {
        every { barRepo.findById(999L) } returns Optional.empty()
        assertThrows(NotFoundException::class.java) { service.getMyBar(999L) }
    }
}
