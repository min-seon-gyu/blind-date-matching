package com.blinddate.marketplace.repository

import com.blinddate.marketplace.entity.MarketplaceAuthorType
import com.blinddate.marketplace.entity.MarketplacePost
import com.blinddate.marketplace.entity.MarketplacePostType
import org.springframework.data.jpa.repository.JpaRepository

interface MarketplacePostRepository : JpaRepository<MarketplacePost, Long> {
    fun findByIsActiveTrue(): List<MarketplacePost>
    fun findByTypeAndIsActiveTrue(type: MarketplacePostType): List<MarketplacePost>
    fun findByRegionAndIsActiveTrue(region: String): List<MarketplacePost>
    fun findByTypeAndRegionAndIsActiveTrue(type: MarketplacePostType, region: String): List<MarketplacePost>
    fun findByAuthorTypeAndAuthorIdAndIsActiveTrue(authorType: MarketplaceAuthorType, authorId: Long): List<MarketplacePost>
}
