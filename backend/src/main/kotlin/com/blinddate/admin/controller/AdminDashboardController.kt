package com.blinddate.admin.controller

import com.blinddate.admin.dto.DashboardResponse
import com.blinddate.bar.repository.BarRepository
import com.blinddate.commission.repository.CommissionRepository
import com.blinddate.event.repository.EventRepository
import com.blinddate.participant.repository.ParticipantRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/dashboard")
class AdminDashboardController(
    private val barRepository: BarRepository,
    private val eventRepository: EventRepository,
    private val participantRepository: ParticipantRepository,
    private val commissionRepository: CommissionRepository
) {
    @GetMapping
    fun getDashboard(): DashboardResponse {
        val allCommissions = commissionRepository.findAll()
        return DashboardResponse(
            totalBars = barRepository.count(),
            activeBars = barRepository.findAll().count { it.isActive }.toLong(),
            totalEvents = eventRepository.count(),
            totalParticipants = participantRepository.count(),
            totalCommissionAmount = allCommissions.sumOf { it.totalAmount.toLong() },
            pendingCommissionAmount = allCommissions.filter {
                it.status == com.blinddate.commission.entity.CommissionStatus.PENDING
            }.sumOf { it.totalAmount.toLong() }
        )
    }
}
