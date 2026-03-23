package com.blinddate.admin.service

import com.blinddate.admin.dto.CreateBarRequest
import com.blinddate.admin.dto.CreateBarOwnerRequest
import com.blinddate.bar.entity.Bar
import com.blinddate.bar.repository.BarRepository
import com.blinddate.barowner.entity.BarOwner
import com.blinddate.barowner.repository.BarOwnerRepository
import com.blinddate.common.exception.ConflictException
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.Optional

class AdminServiceTest {
    private val barRepo = mockk<BarRepository>()
    private val barOwnerRepo = mockk<BarOwnerRepository>()
    private val passwordEncoder = BCryptPasswordEncoder()
    private val service = AdminService(barRepo, barOwnerRepo, passwordEncoder)

    @Test
    fun `createBar should save bar`() {
        every { barRepo.existsBySlug("test-bar") } returns false
        every { barRepo.save(any()) } answers { firstArg() }

        val result = service.createBar(CreateBarRequest(name = "Test", address = "addr", slug = "test-bar"))
        assertEquals("Test", result.name)
        assertEquals("test-bar", result.slug)
    }

    @Test
    fun `createBar should throw for duplicate slug`() {
        every { barRepo.existsBySlug("test-bar") } returns true
        assertThrows(ConflictException::class.java) {
            service.createBar(CreateBarRequest(name = "Test", address = "addr", slug = "test-bar"))
        }
    }

    @Test
    fun `createBarOwner should hash password`() {
        val bar = Bar(name = "Test", address = "addr", slug = "test")
        every { barRepo.findById(1L) } returns Optional.of(bar)
        every { barOwnerRepo.save(any()) } answers { firstArg<BarOwner>() }

        val request = CreateBarOwnerRequest(barId = 1L, name = "Owner", phoneNumber = "010", email = "o@t.com", password = "raw123")
        service.createBarOwner(request)

        verify { barOwnerRepo.save(match { passwordEncoder.matches("raw123", it.password) }) }
    }
}
