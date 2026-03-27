package com.blinddate.marketplace.entity

import com.blinddate.common.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "marketplace_post")
class MarketplacePost(
    @Enumerated(EnumType.STRING) @Column(nullable = false) val type: MarketplacePostType,
    @Enumerated(EnumType.STRING) @Column(nullable = false) val authorType: MarketplaceAuthorType,
    @Column(nullable = false) val authorId: Long,
    @Column(nullable = false) var title: String,
    @Column(columnDefinition = "TEXT", nullable = false) var description: String,
    @Column(nullable = false) var region: String,
    var capacity: Int? = null,
    var preferredDate: String? = null,
    @Column(columnDefinition = "TEXT") var imageUrls: String? = null,
    var cafeId: Long? = null,
    @Column(nullable = false) var isActive: Boolean = true
) : BaseEntity()
