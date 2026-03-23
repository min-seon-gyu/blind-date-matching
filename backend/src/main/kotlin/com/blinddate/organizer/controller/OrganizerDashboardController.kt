package com.blinddate.organizer.controller

import com.blinddate.auth.jwt.UserPrincipal
import com.blinddate.commission.entity.CommissionTargetType
import com.blinddate.commission.repository.CommissionRepository
import com.blinddate.event.entity.EventStatus
import com.blinddate.event.repository.EventRepository
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/organizer/dashboard")
class OrganizerDashboardController(
    private val eventRepository: EventRepository,
    private val commissionRepository: CommissionRepository
) {
    @GetMapping
    fun getDashboard(@AuthenticationPrincipal p: UserPrincipal): Map<String, Any> {
        val events = eventRepository.findByOrganizerIdAndDeletedAtIsNull(p.id)
        val commissions = commissionRepository.findByTargetTypeAndTargetId(CommissionTargetType.ORGANIZER, p.id)
        return mapOf(
            "totalEvents" to events.size,
            "openEvents" to events.count { it.status == EventStatus.OPEN },
            "totalCommissionAmount" to commissions.sumOf { it.totalAmount }
        )
    }
}
