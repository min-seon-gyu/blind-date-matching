package com.blinddate.admin.controller

import com.blinddate.admin.dto.CreateCafeOwnerRequest
import com.blinddate.admin.dto.CreateCafeRequest
import com.blinddate.admin.service.AdminService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin")
class AdminCafeController(private val service: AdminService) {

    @PostMapping("/cafes")
    fun createCafe(@RequestBody request: CreateCafeRequest) = service.createCafe(request)

    @GetMapping("/cafes")
    fun getCafes() = service.getCafes()

    @PostMapping("/cafe-owners")
    fun createCafeOwner(@RequestBody request: CreateCafeOwnerRequest) = service.createCafeOwner(request)
}
