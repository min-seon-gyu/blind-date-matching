package com.blinddate.organizer.controller

import com.blinddate.auth.jwt.UserPrincipal
import com.blinddate.commission.entity.CommissionTargetType
import com.blinddate.commission.service.CommissionService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/organizer/commissions")
class OrganizerCommissionController(private val commissionService: CommissionService) {
    @GetMapping
    fun getCommissions(@AuthenticationPrincipal p: UserPrincipal) =
        commissionService.getByTargetType(CommissionTargetType.ORGANIZER, p.id)
}
