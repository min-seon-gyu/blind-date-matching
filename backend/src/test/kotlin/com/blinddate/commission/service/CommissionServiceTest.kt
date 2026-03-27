package com.blinddate.commission.service

import com.blinddate.application.entity.ApplicationStatus
import com.blinddate.application.repository.ApplicationRepository
import com.blinddate.cafe.entity.Cafe
import com.blinddate.cafeowner.entity.CafeOwner
import com.blinddate.cafeowner.repository.CafeOwnerRepository
import com.blinddate.commission.entity.Commission
import com.blinddate.commission.entity.CommissionStatus
import com.blinddate.commission.entity.CommissionTargetType
import com.blinddate.commission.repository.CommissionRepository
import com.blinddate.common.exception.BadRequestException
import com.blinddate.event.entity.Event
import com.blinddate.event.repository.EventRepository
import com.blinddate.organizer.entity.Organizer
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import java.util.Optional

class CommissionServiceTest {
    private val commissionRepo = mockk<CommissionRepository>()
    private val eventRepo = mockk<EventRepository>()
    private val applicationRepo = mockk<ApplicationRepository>()
    private val cafeOwnerRepo = mockk<CafeOwnerRepository>()
    private val service = CommissionService(commissionRepo, eventRepo, applicationRepo, cafeOwnerRepo)

    private val cafe = Cafe(name = "Test", address = "addr", slug = "test", commissionRate = 10)
    private val organizer = Organizer(name = "Org", phoneNumber = "010", email = "org@test.com", password = "pass", commissionRate = 15)
    private val event = Event(cafe = cafe, organizer = organizer, title = "Friday", date = LocalDate.of(2026, 4, 3),
        time = LocalTime.of(19, 0), price = 30000, maleCapacity = 10, femaleCapacity = 10)

    @Test
    fun `createForEvent should create two commission records`() {
        val cafeOwner = CafeOwner(cafe = cafe, name = "Owner", phoneNumber = "010", email = "o@t.com", password = "p")
        every { eventRepo.findById(1L) } returns Optional.of(event)
        every { commissionRepo.existsByEventId(1L) } returns false
        every { applicationRepo.countByEventIdAndStatus(1L, ApplicationStatus.APPROVED) } returns 16L
        every { cafeOwnerRepo.findByCafeId(cafe.id) } returns listOf(cafeOwner)
        every { commissionRepo.save(any()) } answers { firstArg() }

        val result = service.createForEvent(1L)
        assertEquals(2, result.size)

        // Organizer commission: rate 15, unitPrice = 30000*15/100 = 4500, total = 16*4500 = 72000
        val orgCommission = result.first { it.targetType == CommissionTargetType.ORGANIZER }
        assertEquals(16, orgCommission.participantCount)
        assertEquals(30000, orgCommission.eventPrice)
        assertEquals(15, orgCommission.commissionRate)
        assertEquals(4500, orgCommission.unitPrice)
        assertEquals(72000, orgCommission.totalAmount)

        // Cafe commission: rate 10, unitPrice = 30000*10/100 = 3000, total = 16*3000 = 48000
        val cafeCommission = result.first { it.targetType == CommissionTargetType.CAFE_OWNER }
        assertEquals(10, cafeCommission.commissionRate)
        assertEquals(3000, cafeCommission.unitPrice)
        assertEquals(48000, cafeCommission.totalAmount)
    }

    @Test
    fun `createForEvent should throw if already exists`() {
        every { eventRepo.findById(1L) } returns Optional.of(event)
        every { commissionRepo.existsByEventId(1L) } returns true
        assertThrows(BadRequestException::class.java) { service.createForEvent(1L) }
    }

    @Test
    fun `invoice should change status`() {
        val commission = Commission(cafe = cafe, event = event,
            targetType = CommissionTargetType.ORGANIZER, targetId = 1L,
            participantCount = 10, eventPrice = 30000, commissionRate = 10, unitPrice = 3000, totalAmount = 30000)
        every { commissionRepo.findById(1L) } returns Optional.of(commission)
        service.invoice(1L)
        assertEquals(CommissionStatus.INVOICED, commission.status)
        assertNotNull(commission.invoicedAt)
    }

    @Test
    fun `markPaid should change status`() {
        val commission = Commission(cafe = cafe, event = event,
            targetType = CommissionTargetType.ORGANIZER, targetId = 1L,
            participantCount = 10, eventPrice = 30000, commissionRate = 10, unitPrice = 3000, totalAmount = 30000,
            status = CommissionStatus.INVOICED)
        every { commissionRepo.findById(1L) } returns Optional.of(commission)
        service.markPaid(1L)
        assertEquals(CommissionStatus.PAID, commission.status)
        assertNotNull(commission.paidAt)
    }

    @Test
    fun `markPaid should throw if not INVOICED`() {
        val commission = Commission(cafe = cafe, event = event,
            targetType = CommissionTargetType.ORGANIZER, targetId = 1L,
            participantCount = 10, eventPrice = 30000, commissionRate = 10, unitPrice = 3000, totalAmount = 30000,
            status = CommissionStatus.PENDING)
        every { commissionRepo.findById(1L) } returns Optional.of(commission)
        assertThrows(BadRequestException::class.java) { service.markPaid(1L) }
    }
}
