package com.blinddate.cafeowner.controller

import com.blinddate.auth.jwt.UserPrincipal
import com.blinddate.commission.entity.CommissionTargetType
import com.blinddate.commission.service.CommissionService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/cafe-owner/commissions")
class CafeOwnerCommissionController(private val commissionService: CommissionService) {
    @GetMapping
    fun getCommissions(@AuthenticationPrincipal p: UserPrincipal) =
        commissionService.getByTargetType(CommissionTargetType.CAFE_OWNER, p.id)
}
