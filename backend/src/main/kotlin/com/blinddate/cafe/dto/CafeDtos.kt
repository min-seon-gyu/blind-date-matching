package com.blinddate.cafe.dto

import com.blinddate.cafe.entity.Cafe

data class CafeResponse(
    val id: Long,
    val name: String,
    val address: String,
    val description: String,
    val logoUrl: String,
    val coverImageUrl: String,
    val slug: String,
    val isActive: Boolean
) {
    companion object {
        fun from(cafe: Cafe) = CafeResponse(
            id = cafe.id, name = cafe.name, address = cafe.address,
            description = cafe.description, logoUrl = cafe.logoUrl,
            coverImageUrl = cafe.coverImageUrl, slug = cafe.slug, isActive = cafe.isActive
        )
    }
}
