package com.blinddate.marketplace.controller

import com.blinddate.auth.jwt.UserPrincipal
import com.blinddate.marketplace.dto.CreateMarketplacePostRequest
import com.blinddate.marketplace.dto.UpdateMarketplacePostRequest
import com.blinddate.marketplace.entity.MarketplacePostType
import com.blinddate.marketplace.service.MarketplaceService
import com.blinddate.partnership.dto.RequestPartnershipRequest
import com.blinddate.partnership.service.PartnershipService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/marketplace/posts")
class MarketplaceController(
    private val marketplaceService: MarketplaceService,
    private val partnershipService: PartnershipService
) {
    @GetMapping
    fun getPosts(
        @RequestParam(required = false) type: MarketplacePostType?,
        @RequestParam(required = false) region: String?
    ) = marketplaceService.getPosts(type, region)

    @PostMapping
    fun createPost(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: CreateMarketplacePostRequest
    ) = marketplaceService.createPost(principal, request)

    @GetMapping("/{id}")
    fun getPost(@PathVariable id: Long) = marketplaceService.getPost(id)

    @PutMapping("/{id}")
    fun updatePost(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: Long,
        @RequestBody request: UpdateMarketplacePostRequest
    ) = marketplaceService.updatePost(principal, id, request)

    @DeleteMapping("/{id}")
    fun deletePost(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: Long
    ) = marketplaceService.deletePost(principal, id)

    @PostMapping("/{id}/request-partnership")
    fun requestPartnership(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: Long,
        @RequestBody(required = false) request: RequestPartnershipRequest?
    ) = partnershipService.requestPartnership(
        principal,
        marketplaceService.getPostEntity(id),
        request?.message
    )

    @GetMapping("/my")
    fun getMyPosts(@AuthenticationPrincipal principal: UserPrincipal) =
        marketplaceService.getMyPosts(principal)
}
