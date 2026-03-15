package com.blinddate.matching.controller

import com.blinddate.matching.dto.MatchResultResponse
import com.blinddate.matching.dto.ParticipantNumberResponse
import com.blinddate.matching.dto.SubmitChoicesRequest
import com.blinddate.matching.service.MatchingService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/events/{eventId}")
class MatchingController(
    private val matchingService: MatchingService
) {

    @GetMapping("/participants")
    fun getParticipants(
        @PathVariable eventId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<List<ParticipantNumberResponse>> {
        val memberId = userDetails.username.toLong()
        return ResponseEntity.ok(matchingService.getParticipants(eventId, memberId))
    }

    @PostMapping("/choices")
    fun submitChoices(
        @PathVariable eventId: Long,
        @RequestBody request: SubmitChoicesRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Void> {
        val memberId = userDetails.username.toLong()
        matchingService.submitChoices(eventId, memberId, request.chosenMemberIds)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/match-result")
    fun getMatchResult(
        @PathVariable eventId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<List<MatchResultResponse>> {
        val memberId = userDetails.username.toLong()
        return ResponseEntity.ok(matchingService.getMatchResult(eventId, memberId))
    }
}
