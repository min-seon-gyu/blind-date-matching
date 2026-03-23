package com.blinddate.admin.controller

import com.blinddate.admin.dto.CreateBarOwnerRequest
import com.blinddate.admin.dto.CreateBarRequest
import com.blinddate.admin.service.AdminService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin")
class AdminBarController(private val service: AdminService) {

    @PostMapping("/bars")
    fun createBar(@RequestBody request: CreateBarRequest) = service.createBar(request)

    @GetMapping("/bars")
    fun getBars() = service.getBars()

    @PostMapping("/bar-owners")
    fun createBarOwner(@RequestBody request: CreateBarOwnerRequest) = service.createBarOwner(request)
}
