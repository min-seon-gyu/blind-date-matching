package com.blinddate.commission.service

import com.blinddate.application.entity.ApplicationStatus
import com.blinddate.application.repository.ApplicationRepository
import com.blinddate.bar.entity.Bar
import com.blinddate.commission.entity.Commission
import com.blinddate.commission.entity.CommissionStatus
import com.blinddate.commission.repository.CommissionRepository
import com.blinddate.common.exception.BadRequestException
import com.blinddate.common.exception.NotFoundException
import com.blinddate.event.entity.Event
import com.blinddate.event.repository.EventRepository
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
    private val service = CommissionService(commissionRepo, eventRepo, applicationRepo)

    private val bar = Bar(name = "Test", address = "addr", slug = "test", commissionRate = 10)
    private val event = Event(bar = bar, title = "Friday", date = LocalDate.of(2026, 4, 3),
        time = LocalTime.of(19, 0), price = 30000, maleCapacity = 10, femaleCapacity = 10)

    @Test
    fun `createForEvent should calculate commission correctly`() {
        every { eventRepo.findById(1L) } returns Optional.of(event)
        every { commissionRepo.existsByEventId(1L) } returns false
        every { applicationRepo.countByEventIdAndStatus(1L, ApplicationStatus.APPROVED) } returns 16L
        every { commissionRepo.save(any()) } answers { firstArg() }

        val result = service.createForEvent(1L)
        assertEquals(16, result.participantCount)
        assertEquals(30000, result.eventPrice)
        assertEquals(10, result.commissionRate)
        assertEquals(3000, result.unitPrice)  // 30000 * 10 / 100
        assertEquals(48000, result.totalAmount)  // 16 * 3000
    }

    @Test
    fun `createForEvent should throw if already exists`() {
        every { eventRepo.findById(1L) } returns Optional.of(event)
        every { commissionRepo.existsByEventId(1L) } returns true
        assertThrows(BadRequestException::class.java) { service.createForEvent(1L) }
    }

    @Test
    fun `invoice should change status`() {
        val commission = Commission(bar = bar, event = event, participantCount = 10,
            eventPrice = 30000, commissionRate = 10, unitPrice = 3000, totalAmount = 30000)
        every { commissionRepo.findById(1L) } returns Optional.of(commission)
        service.invoice(1L)
        assertEquals(CommissionStatus.INVOICED, commission.status)
        assertNotNull(commission.invoicedAt)
    }

    @Test
    fun `markPaid should change status`() {
        val commission = Commission(bar = bar, event = event, participantCount = 10,
            eventPrice = 30000, commissionRate = 10, unitPrice = 3000, totalAmount = 30000,
            status = CommissionStatus.INVOICED)
        every { commissionRepo.findById(1L) } returns Optional.of(commission)
        service.markPaid(1L)
        assertEquals(CommissionStatus.PAID, commission.status)
        assertNotNull(commission.paidAt)
    }

    @Test
    fun `markPaid should throw if not INVOICED`() {
        val commission = Commission(bar = bar, event = event, participantCount = 10,
            eventPrice = 30000, commissionRate = 10, unitPrice = 3000, totalAmount = 30000,
            status = CommissionStatus.PENDING)
        every { commissionRepo.findById(1L) } returns Optional.of(commission)
        assertThrows(BadRequestException::class.java) { service.markPaid(1L) }
    }
}
