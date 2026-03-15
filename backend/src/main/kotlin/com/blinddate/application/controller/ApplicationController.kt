package com.blinddate.application.controller

import com.blinddate.application.dto.ApplicationResponse
import com.blinddate.application.service.ApplicationService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
class ApplicationController(private val applicationService: ApplicationService) {

    private fun currentMemberId(): Long = SecurityContextHolder.getContext().authentication.principal as Long

    @PostMapping("/api/events/{id}/apply")
    fun apply(@PathVariable id: Long): ResponseEntity<ApplicationResponse> =
        ResponseEntity.ok(applicationService.apply(currentMemberId(), id))

    @DeleteMapping("/api/applications/{id}")
    fun cancel(@PathVariable id: Long): ResponseEntity<Void> {
        applicationService.cancel(currentMemberId(), id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/api/members/me/applications")
    fun getMyApplications(): ResponseEntity<List<ApplicationResponse>> =
        ResponseEntity.ok(applicationService.getMyApplications(currentMemberId()))
}
