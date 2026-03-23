package com.blinddate.admin.controller

import com.blinddate.commission.service.CommissionService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/commissions")
class AdminCommissionController(private val commissionService: CommissionService) {

    @GetMapping
    fun getAll() = commissionService.getAll()

    @PutMapping("/{id}/invoice")
    fun invoice(@PathVariable id: Long) = commissionService.invoice(id)

    @PutMapping("/{id}/paid")
    fun markPaid(@PathVariable id: Long) = commissionService.markPaid(id)
}
