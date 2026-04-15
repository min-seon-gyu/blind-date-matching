package com.blinddate.organizer.controller

import com.blinddate.auth.jwt.UserPrincipal
import com.blinddate.partnership.service.PartnershipService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/organizer/partnerships")
class OrganizerPartnershipController(private val partnershipService: PartnershipService) {

    @GetMapping
    fun getPartnerships(@AuthenticationPrincipal principal: UserPrincipal) =
        partnershipService.getByOrganizerId(principal.id)
}
