package com.blinddate.action.controller

import com.blinddate.action.service.ActionTokenService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/actions")
class ActionController(private val actionTokenService: ActionTokenService) {

    @GetMapping("/{token}")
    fun getActionInfo(@PathVariable token: String) = actionTokenService.getActionInfo(token)

    @PostMapping("/{token}/execute")
    fun executeAction(@PathVariable token: String) = actionTokenService.executeAction(token)
}
