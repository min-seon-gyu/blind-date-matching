package com.blinddate.matching.controller

import com.blinddate.auth.jwt.UserPrincipal
import com.blinddate.matching.dto.ChoiceRequest
import com.blinddate.matching.service.MatchingService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
class MatchingController(private val matchingService: MatchingService) {

    @GetMapping("/api/events/{eventId}/participants")
    fun getParticipants(@AuthenticationPrincipal principal: UserPrincipal, @PathVariable eventId: Long) =
        matchingService.getParticipants(eventId, principal.id)

    @PostMapping("/api/events/{eventId}/choices")
    fun submitChoices(@AuthenticationPrincipal principal: UserPrincipal, @PathVariable eventId: Long, @RequestBody request: ChoiceRequest) =
        matchingService.submitChoices(eventId, principal.id, request.chosenParticipantIds)

    @PutMapping("/api/events/{eventId}/choices")
    fun updateChoices(@AuthenticationPrincipal principal: UserPrincipal, @PathVariable eventId: Long, @RequestBody request: ChoiceRequest) =
        matchingService.submitChoices(eventId, principal.id, request.chosenParticipantIds)

    @GetMapping("/api/events/{eventId}/result")
    fun getResult(@AuthenticationPrincipal principal: UserPrincipal, @PathVariable eventId: Long) =
        matchingService.getMatchResult(eventId, principal.id)
}
