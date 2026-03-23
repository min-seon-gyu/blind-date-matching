package com.blinddate.cafeowner.dto

data class UpdateCafeRequest(
    val name: String,
    val address: String,
    val description: String = "",
    val logoUrl: String = "",
    val coverImageUrl: String = ""
)
