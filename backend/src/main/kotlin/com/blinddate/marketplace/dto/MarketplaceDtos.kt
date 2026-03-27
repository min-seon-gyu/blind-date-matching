package com.blinddate.marketplace.dto

import com.blinddate.marketplace.entity.MarketplacePostType

data class CreateMarketplacePostRequest(
    val title: String,
    val description: String,
    val region: String,
    val capacity: Int? = null,
    val preferredDate: String? = null,
    val imageUrls: String? = null
)

data class UpdateMarketplacePostRequest(
    val title: String,
    val description: String,
    val region: String,
    val capacity: Int? = null,
    val preferredDate: String? = null,
    val imageUrls: String? = null
)

data class MarketplacePostResponse(
    val id: Long,
    val type: MarketplacePostType,
    val authorName: String,
    val authorId: Long,
    val title: String,
    val description: String,
    val region: String,
    val capacity: Int?,
    val preferredDate: String?,
    val imageUrls: String?,
    val cafeId: Long?,
    val cafeAddress: String?,
    val cafeLatitude: Double?,
    val cafeLongitude: Double?,
    val isActive: Boolean
)
