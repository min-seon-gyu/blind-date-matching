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

    @Test
    fun `getByTargetType returns filtered commissions`() {
        val commission = Commission(
            cafe = cafe, event = event,
            targetType = CommissionTargetType.ORGANIZER, targetId = 1L,
            participantCount = 10, eventPrice = 30000, commissionRate = 15,
            unitPrice = 4500, totalAmount = 45000
        )
        every { commissionRepo.findByTargetTypeAndTargetId(CommissionTargetType.ORGANIZER, 1L) } returns listOf(commission)

        val result = service.getByTargetType(CommissionTargetType.ORGANIZER, 1L)

        assertEquals(1, result.size)
        assertEquals(CommissionTargetType.ORGANIZER, result[0].targetType)
        assertEquals(45000, result[0].totalAmount)
    }

    @Test
    fun `createForEvent calculates correct amounts for different rates`() {
        val highRateCafe = Cafe(name = "High", address = "addr", slug = "high", commissionRate = 20)
        val highRateOrganizer = Organizer(name = "Org", phoneNumber = "010", email = "org@test.com", password = "pass", commissionRate = 25)
        val highRateEvent = Event(
            cafe = highRateCafe, organizer = highRateOrganizer, title = "Premium",
            date = LocalDate.of(2026, 4, 3), time = LocalTime.of(19, 0),
            price = 50000, maleCapacity = 10, femaleCapacity = 10
        )
        val cafeOwner = CafeOwner(cafe = highRateCafe, name = "Owner", phoneNumber = "010", email = "o@t.com", password = "p")

        every { eventRepo.findById(highRateEvent.id) } returns Optional.of(highRateEvent)
        every { commissionRepo.existsByEventId(highRateEvent.id) } returns false
        every { applicationRepo.countByEventIdAndStatus(highRateEvent.id, ApplicationStatus.APPROVED) } returns 20L
        every { cafeOwnerRepo.findByCafeId(highRateCafe.id) } returns listOf(cafeOwner)
        every { commissionRepo.save(any()) } answers { firstArg() }

        val result = service.createForEvent(highRateEvent.id)

        assertEquals(2, result.size)

        // Organizer: rate 25, unitPrice = 50000*25/100 = 12500, total = 20*12500 = 250000
        val orgCommission = result.first { it.targetType == CommissionTargetType.ORGANIZER }
        assertEquals(25, orgCommission.commissionRate)
        assertEquals(12500, orgCommission.unitPrice)
        assertEquals(250000, orgCommission.totalAmount)

        // Cafe: rate 20, unitPrice = 50000*20/100 = 10000, total = 20*10000 = 200000
        val cafeCommission = result.first { it.targetType == CommissionTargetType.CAFE_OWNER }
        assertEquals(20, cafeCommission.commissionRate)
        assertEquals(10000, cafeCommission.unitPrice)
        assertEquals(200000, cafeCommission.totalAmount)
    }
}
