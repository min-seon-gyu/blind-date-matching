package com.blinddate.application.controller

import com.blinddate.application.dto.ApplicationDetailResponse
import com.blinddate.application.dto.ApplicationResponse
import com.blinddate.application.dto.RejectRequest
import com.blinddate.application.service.ApplicationService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/applications")
class AdminApplicationController(private val applicationService: ApplicationService) {

    private fun currentMemberId(): Long = SecurityContextHolder.getContext().authentication.principal as Long

    @GetMapping
    fun getAllApplications(): ResponseEntity<List<ApplicationResponse>> =
        ResponseEntity.ok(applicationService.getAllApplications())

    @GetMapping("/{id}")
    fun getApplicationDetail(@PathVariable id: Long): ResponseEntity<ApplicationDetailResponse> =
        ResponseEntity.ok(applicationService.getApplicationDetail(id))

    @PutMapping("/{id}/approve")
    fun approve(@PathVariable id: Long): ResponseEntity<ApplicationResponse> =
        ResponseEntity.ok(applicationService.approve(currentMemberId(), id))

    @PutMapping("/{id}/reject")
    fun reject(@PathVariable id: Long, @RequestBody request: RejectRequest): ResponseEntity<ApplicationResponse> =
        ResponseEntity.ok(applicationService.reject(currentMemberId(), id, request.reason))
}
