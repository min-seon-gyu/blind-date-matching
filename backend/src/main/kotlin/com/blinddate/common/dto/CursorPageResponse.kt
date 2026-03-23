package com.blinddate.common.dto

data class CursorPageResponse<T>(
    val content: List<T>,
    val nextCursor: Long?,
    val hasNext: Boolean
)
