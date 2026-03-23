package com.blinddate.bar.dto
data class BarResponse(
    val id: Long, val name: String, val address: String, val description: String,
    val logoUrl: String, val coverImageUrl: String, val slug: String, val isActive: Boolean
)
