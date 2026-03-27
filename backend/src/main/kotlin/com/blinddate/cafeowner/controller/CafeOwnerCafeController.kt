package com.blinddate.cafeowner.controller

import com.blinddate.auth.jwt.UserPrincipal
import com.blinddate.cafeowner.dto.UpdateCafeRequest
import com.blinddate.cafeowner.service.CafeOwnerService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/cafe-owner")
class CafeOwnerCafeController(private val service: CafeOwnerService) {

    @GetMapping("/my-cafe")
    fun getMyCafe(@AuthenticationPrincipal principal: UserPrincipal) = service.getMyCafe(principal.cafeId!!)

    @PutMapping("/my-cafe")
    fun updateMyCafe(@AuthenticationPrincipal principal: UserPrincipal, @RequestBody request: UpdateCafeRequest) =
        service.updateMyCafe(principal.cafeId!!, request)
}
