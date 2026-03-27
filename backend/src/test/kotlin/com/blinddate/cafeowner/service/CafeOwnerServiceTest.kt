package com.blinddate.cafeowner.service

import com.blinddate.cafe.entity.Cafe
import com.blinddate.cafe.repository.CafeRepository
import com.blinddate.cafeowner.dto.UpdateCafeRequest
import com.blinddate.common.exception.NotFoundException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.Optional

class CafeOwnerServiceTest {

    private val cafeRepo = mockk<CafeRepository>()
    private val service = CafeOwnerService(cafeRepo)

    private val cafe = Cafe(name = "Test Cafe", address = "서울시 강남구", slug = "test-cafe", description = "설명", logoUrl = "logo.png", coverImageUrl = "cover.png")

    @Test
    fun `getMyCafe returns cafe info`() {
        every { cafeRepo.findById(1L) } returns Optional.of(cafe)

        val result = service.getMyCafe(1L)

        assertEquals("Test Cafe", result.name)
        assertEquals("서울시 강남구", result.address)
        assertEquals("test-cafe", result.slug)
    }

    @Test
    fun `getMyCafe throws NotFoundException when cafe not found`() {
        every { cafeRepo.findById(99L) } returns Optional.empty()

        assertThrows(NotFoundException::class.java) {
            service.getMyCafe(99L)
        }
    }

    @Test
    fun `updateMyCafe updates all fields`() {
        every { cafeRepo.findById(1L) } returns Optional.of(cafe)

        val request = UpdateCafeRequest(
            name = "Updated Cafe",
            address = "서울시 서초구",
            description = "새로운 설명",
            logoUrl = "new-logo.png",
            coverImageUrl = "new-cover.png"
        )

        val result = service.updateMyCafe(1L, request)

        assertEquals("Updated Cafe", result.name)
        assertEquals("서울시 서초구", result.address)
        assertEquals("새로운 설명", result.description)
        assertEquals("new-logo.png", result.logoUrl)
        assertEquals("new-cover.png", result.coverImageUrl)
    }

    @Test
    fun `updateMyCafe with default empty strings for optional fields`() {
        every { cafeRepo.findById(1L) } returns Optional.of(cafe)

        val request = UpdateCafeRequest(
            name = "Minimal Update",
            address = "서울시 강남구"
        )

        val result = service.updateMyCafe(1L, request)

        assertEquals("Minimal Update", result.name)
        assertEquals("서울시 강남구", result.address)
        assertEquals("", result.description)
        assertEquals("", result.logoUrl)
        assertEquals("", result.coverImageUrl)
    }
}
