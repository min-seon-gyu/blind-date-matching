package com.blinddate.cafe.service

import com.blinddate.cafe.entity.Cafe
import com.blinddate.cafe.repository.CafeRepository
import com.blinddate.common.exception.NotFoundException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CafeServiceTest {

    private val cafeRepo = mockk<CafeRepository>()
    private val service = CafeService(cafeRepo)

    private val cafe = Cafe(name = "Test Cafe", address = "서울시 강남구", slug = "test-cafe")

    @Test
    fun `getBySlug returns cafe`() {
        every { cafeRepo.findBySlug("test-cafe") } returns cafe

        val result = service.getBySlug("test-cafe")

        assertEquals("Test Cafe", result.name)
        assertEquals("test-cafe", result.slug)
        assertEquals("서울시 강남구", result.address)
    }

    @Test
    fun `getBySlug throws NotFoundException for unknown slug`() {
        every { cafeRepo.findBySlug("unknown") } returns null

        assertThrows(NotFoundException::class.java) {
            service.getBySlug("unknown")
        }
    }
}
