package com.blinddate.barowner.controller

import com.blinddate.auth.jwt.UserPrincipal
import com.blinddate.barowner.dto.BarUpdateRequest
import com.blinddate.barowner.service.BarOwnerService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/bar-owner")
class BarOwnerBarController(private val service: BarOwnerService) {

    @GetMapping("/my-bar")
    fun getMyBar(@AuthenticationPrincipal principal: UserPrincipal) = service.getMyBar(principal.barId!!)

    @PutMapping("/my-bar")
    fun updateMyBar(@AuthenticationPrincipal principal: UserPrincipal, @RequestBody request: BarUpdateRequest) =
        service.updateMyBar(principal.barId!!, request)
}
