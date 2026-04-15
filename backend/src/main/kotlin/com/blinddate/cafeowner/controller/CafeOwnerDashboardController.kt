package com.blinddate.cafeowner.controller

import com.blinddate.auth.jwt.UserPrincipal
import com.blinddate.commission.entity.CommissionTargetType
import com.blinddate.commission.repository.CommissionRepository
import com.blinddate.event.entity.EventStatus
import com.blinddate.event.repository.EventRepository
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/cafe-owner/dashboard")
class CafeOwnerDashboardController(
    private val eventRepository: EventRepository,
    private val commissionRepository: CommissionRepository
) {
    @GetMapping
    fun getDashboard(@AuthenticationPrincipal p: UserPrincipal): Map<String, Any> {
        val events = eventRepository.findByCafeIdAndDeletedAtIsNull(p.cafeId!!)
        val commissions = commissionRepository.findByTargetTypeAndTargetId(CommissionTargetType.CAFE_OWNER, p.id)
        return mapOf(
            "totalEvents" to events.size,
            "openEvents" to events.count { it.status == EventStatus.OPEN },
            "totalCommissionAmount" to commissions.sumOf { it.totalAmount }
        )
    }
}
