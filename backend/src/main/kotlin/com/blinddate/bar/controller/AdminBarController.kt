package com.blinddate.bar.controller

import com.blinddate.bar.dto.CheckInRequest
import com.blinddate.bar.dto.VisitorResponse
import com.blinddate.bar.service.BarService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/bar")
class AdminBarController(private val barService: BarService) {

    @PutMapping("/open")
    fun openBar(@RequestParam(defaultValue = "1") barId: Long): ResponseEntity<Void> {
        barService.openBar(barId)
        return ResponseEntity.ok().build()
    }

    @PutMapping("/close")
    fun closeBar(@RequestParam(defaultValue = "1") barId: Long): ResponseEntity<Void> {
        barService.closeBar(barId)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/check-in")
    fun checkIn(
        @RequestParam(defaultValue = "1") barId: Long,
        @RequestBody request: CheckInRequest
    ): ResponseEntity<Void> {
        barService.checkIn(barId, request)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/check-out/{visitLogId}")
    fun checkOut(@PathVariable visitLogId: Long): ResponseEntity<Void> {
        barService.checkOut(visitLogId)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/visitors")
    fun getCurrentVisitors(@RequestParam(defaultValue = "1") barId: Long): ResponseEntity<List<VisitorResponse>> =
        ResponseEntity.ok(barService.getCurrentVisitors(barId))
}
