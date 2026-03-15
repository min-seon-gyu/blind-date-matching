package com.blinddate.bar.controller

import com.blinddate.bar.dto.BarReservationResponse
import com.blinddate.bar.dto.BarReserveRequest
import com.blinddate.bar.dto.BarStatusResponse
import com.blinddate.bar.service.BarService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/bar")
class BarController(private val barService: BarService) {

    private fun currentMemberId(): Long = SecurityContextHolder.getContext().authentication.principal as Long

    @GetMapping("/status")
    fun getStatus(@RequestParam(defaultValue = "1") barId: Long): ResponseEntity<BarStatusResponse> =
        ResponseEntity.ok(barService.getStatus(barId))

    @PostMapping("/reservations")
    fun reserve(
        @RequestParam(defaultValue = "1") barId: Long,
        @RequestBody request: BarReserveRequest
    ): ResponseEntity<BarReservationResponse> =
        ResponseEntity.ok(barService.reserve(currentMemberId(), barId, request))

    @DeleteMapping("/reservations/{id}")
    fun cancelReservation(@PathVariable id: Long): ResponseEntity<Void> {
        barService.cancelReservation(currentMemberId(), id)
        return ResponseEntity.noContent().build()
    }
}
