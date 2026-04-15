package com.blinddate.admin.controller

import com.blinddate.partnership.service.PartnershipService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/partnerships")
class AdminPartnershipController(private val partnershipService: PartnershipService) {

    @GetMapping
    fun getAllPartnerships() = partnershipService.getAll()
}
