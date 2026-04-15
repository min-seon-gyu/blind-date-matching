package com.blinddate.admin.controller

import com.blinddate.admin.dto.CreateOrganizerRequest
import com.blinddate.admin.service.AdminService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin")
class AdminOrganizerController(private val service: AdminService) {

    @PostMapping("/organizers")
    fun createOrganizer(@RequestBody request: CreateOrganizerRequest) = service.createOrganizer(request)

    @GetMapping("/organizers")
    fun getOrganizers() = service.getOrganizers()
}
