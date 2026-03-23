package com.blinddate.action.dto

data class ActionInfoResponse(
    val actionType: String,
    val targetId: Long,
    val expired: Boolean,
    val used: Boolean
)

data class ActionExecuteResponse(
    val success: Boolean,
    val message: String
)
