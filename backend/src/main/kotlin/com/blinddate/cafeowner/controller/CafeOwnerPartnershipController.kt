package com.blinddate.cafeowner.controller

import com.blinddate.auth.jwt.UserPrincipal
import com.blinddate.partnership.service.PartnershipService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/cafe-owner/partnerships")
class CafeOwnerPartnershipController(private val partnershipService: PartnershipService) {

    @GetMapping
    fun getPartnerships(@AuthenticationPrincipal principal: UserPrincipal) =
        partnershipService.getByCafeId(principal.cafeId!!)

    @PutMapping("/{id}/accept")
    fun accept(@AuthenticationPrincipal principal: UserPrincipal, @PathVariable id: Long) =
        partnershipService.accept(id, principal.cafeId!!)

    @PutMapping("/{id}/reject")
    fun reject(@AuthenticationPrincipal principal: UserPrincipal, @PathVariable id: Long) =
        partnershipService.reject(id, principal.cafeId!!)

    @PutMapping("/{id}/terminate")
    fun terminate(@AuthenticationPrincipal principal: UserPrincipal, @PathVariable id: Long) =
        partnershipService.terminate(id, principal.cafeId!!)
}
