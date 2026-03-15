package com.blinddate.member.controller

import com.blinddate.bar.dto.BarReservationResponse
import com.blinddate.bar.service.BarService
import com.blinddate.member.dto.*
import com.blinddate.member.service.MemberService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/members")
class MemberController(
    private val memberService: MemberService,
    private val barService: BarService
) {
    private fun currentMemberId(): Long = SecurityContextHolder.getContext().authentication.principal as Long

    @GetMapping("/me")
    fun getMe(): ResponseEntity<MemberResponse> = ResponseEntity.ok(memberService.getMe(currentMemberId()))

    @PostMapping("/me/profile")
    fun createProfile(@Valid @RequestBody request: ProfileCreateRequest): ResponseEntity<ProfileResponse> =
        ResponseEntity.ok(memberService.createProfile(currentMemberId(), request))

    @GetMapping("/me/profile")
    fun getProfile(): ResponseEntity<ProfileResponse> = ResponseEntity.ok(memberService.getProfile(currentMemberId()))

    @PutMapping("/me/profile")
    fun updateProfile(@Valid @RequestBody request: ProfileCreateRequest): ResponseEntity<ProfileResponse> =
        ResponseEntity.ok(memberService.updateProfile(currentMemberId(), request))

    @GetMapping("/me/bar-reservations")
    fun getMyBarReservations(): ResponseEntity<List<BarReservationResponse>> =
        ResponseEntity.ok(barService.getMyReservations(currentMemberId()))
}
