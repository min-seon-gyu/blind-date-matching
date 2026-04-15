package com.blinddate.admin.service

import com.blinddate.admin.dto.CreateCafeOwnerRequest
import com.blinddate.admin.dto.CreateCafeRequest
import com.blinddate.admin.dto.CreateOrganizerRequest
import com.blinddate.cafe.entity.Cafe
import com.blinddate.cafe.repository.CafeRepository
import com.blinddate.cafeowner.entity.CafeOwner
import com.blinddate.cafeowner.repository.CafeOwnerRepository
import com.blinddate.common.exception.ConflictException
import com.blinddate.organizer.entity.Organizer
import com.blinddate.organizer.repository.OrganizerRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.Optional

class AdminServiceTest {
    private val cafeRepo = mockk<CafeRepository>()
    private val cafeOwnerRepo = mockk<CafeOwnerRepository>()
    private val organizerRepo = mockk<OrganizerRepository>()
    private val passwordEncoder = BCryptPasswordEncoder()
    private val service = AdminService(cafeRepo, cafeOwnerRepo, organizerRepo, passwordEncoder)

    @Test
    fun `createCafe should save cafe`() {
        every { cafeRepo.existsBySlug("test-cafe") } returns false
        every { cafeRepo.save(any()) } answers { firstArg() }

        val result = service.createCafe(CreateCafeRequest(name = "Test", address = "addr", slug = "test-cafe"))
        assertEquals("Test", result.name)
        assertEquals("test-cafe", result.slug)
    }

    @Test
    fun `createCafe should throw for duplicate slug`() {
        every { cafeRepo.existsBySlug("test-cafe") } returns true
        assertThrows(ConflictException::class.java) {
            service.createCafe(CreateCafeRequest(name = "Test", address = "addr", slug = "test-cafe"))
        }
    }

    @Test
    fun `createCafeOwner should hash password`() {
        val cafe = Cafe(name = "Test", address = "addr", slug = "test")
        every { cafeRepo.findById(1L) } returns Optional.of(cafe)
        every { cafeOwnerRepo.save(any()) } answers { firstArg<CafeOwner>() }

        val request = CreateCafeOwnerRequest(cafeId = 1L, name = "Owner", phoneNumber = "010", email = "o@t.com", password = "raw123")
        service.createCafeOwner(request)

        verify { cafeOwnerRepo.save(match { passwordEncoder.matches("raw123", it.password) }) }
    }

    @Test
    fun `createOrganizer should save organizer`() {
        every { organizerRepo.save(any()) } answers { firstArg<Organizer>() }

        val request = CreateOrganizerRequest(name = "Org", phoneNumber = "010", email = "org@t.com", password = "pass123")
        val result = service.createOrganizer(request)
        assertEquals("Org", result.name)
    }
}
