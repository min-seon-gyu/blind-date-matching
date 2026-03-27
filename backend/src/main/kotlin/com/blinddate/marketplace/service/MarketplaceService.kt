package com.blinddate.marketplace.service

import com.blinddate.auth.jwt.UserPrincipal
import com.blinddate.auth.jwt.UserType
import com.blinddate.cafe.repository.CafeRepository
import com.blinddate.cafeowner.repository.CafeOwnerRepository
import com.blinddate.common.exception.BadRequestException
import com.blinddate.common.exception.ForbiddenException
import com.blinddate.common.exception.NotFoundException
import com.blinddate.marketplace.dto.CreateMarketplacePostRequest
import com.blinddate.marketplace.dto.MarketplacePostResponse
import com.blinddate.marketplace.dto.UpdateMarketplacePostRequest
import com.blinddate.marketplace.entity.MarketplaceAuthorType
import com.blinddate.marketplace.entity.MarketplacePost
import com.blinddate.marketplace.entity.MarketplacePostType
import com.blinddate.marketplace.repository.MarketplacePostRepository
import com.blinddate.organizer.repository.OrganizerRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class MarketplaceService(
    private val postRepository: MarketplacePostRepository,
    private val cafeOwnerRepository: CafeOwnerRepository,
    private val organizerRepository: OrganizerRepository,
    private val cafeRepository: CafeRepository
) {
    @Transactional
    fun createPost(principal: UserPrincipal, request: CreateMarketplacePostRequest): MarketplacePostResponse {
        val type: MarketplacePostType
        val authorType: MarketplaceAuthorType
        var cafeId: Long? = null

        when (principal.userType) {
            UserType.CAFE_OWNER -> {
                type = MarketplacePostType.OFFER_SPACE
                authorType = MarketplaceAuthorType.CAFE_OWNER
                cafeId = principal.cafeId ?: throw BadRequestException("카페 정보가 없습니다")
            }
            UserType.ORGANIZER -> {
                type = MarketplacePostType.SEEK_SPACE
                authorType = MarketplaceAuthorType.ORGANIZER
            }
            else -> throw ForbiddenException("카페 오너 또는 주최자만 게시글을 작성할 수 있습니다")
        }

        val post = postRepository.save(
            MarketplacePost(
                type = type,
                authorType = authorType,
                authorId = principal.id,
                title = request.title,
                description = request.description,
                region = request.region,
                capacity = request.capacity,
                preferredDate = request.preferredDate,
                imageUrls = request.imageUrls,
                cafeId = cafeId
            )
        )
        return toResponse(post)
    }

    @Transactional
    fun updatePost(principal: UserPrincipal, postId: Long, request: UpdateMarketplacePostRequest): MarketplacePostResponse {
        val post = getActivePost(postId)
        validateOwnership(principal, post)

        post.title = request.title
        post.description = request.description
        post.region = request.region
        post.capacity = request.capacity
        post.preferredDate = request.preferredDate
        post.imageUrls = request.imageUrls

        return toResponse(post)
    }

    @Transactional
    fun deletePost(principal: UserPrincipal, postId: Long) {
        val post = getActivePost(postId)
        validateOwnership(principal, post)
        post.isActive = false
    }

    fun getPosts(type: MarketplacePostType?, region: String?): List<MarketplacePostResponse> {
        val posts = when {
            type != null && region != null -> postRepository.findByTypeAndRegionAndIsActiveTrue(type, region)
            type != null -> postRepository.findByTypeAndIsActiveTrue(type)
            region != null -> postRepository.findByRegionAndIsActiveTrue(region)
            else -> postRepository.findByIsActiveTrue()
        }
        return posts.map { toResponse(it) }
    }

    fun getPost(postId: Long): MarketplacePostResponse {
        val post = getActivePost(postId)
        return toResponse(post)
    }

    fun getMyPosts(principal: UserPrincipal): List<MarketplacePostResponse> {
        val authorType = when (principal.userType) {
            UserType.CAFE_OWNER -> MarketplaceAuthorType.CAFE_OWNER
            UserType.ORGANIZER -> MarketplaceAuthorType.ORGANIZER
            else -> throw ForbiddenException("권한이 없습니다")
        }
        return postRepository.findByAuthorTypeAndAuthorIdAndIsActiveTrue(authorType, principal.id)
            .map { toResponse(it) }
    }

    fun getPostEntity(postId: Long): MarketplacePost = getActivePost(postId)

    private fun getActivePost(postId: Long): MarketplacePost {
        val post = postRepository.findById(postId).orElseThrow { NotFoundException("게시글을 찾을 수 없습니다") }
        if (!post.isActive) throw NotFoundException("삭제된 게시글입니다")
        return post
    }

    private fun validateOwnership(principal: UserPrincipal, post: MarketplacePost) {
        val expectedAuthorType = when (principal.userType) {
            UserType.CAFE_OWNER -> MarketplaceAuthorType.CAFE_OWNER
            UserType.ORGANIZER -> MarketplaceAuthorType.ORGANIZER
            else -> throw ForbiddenException("권한이 없습니다")
        }
        if (post.authorType != expectedAuthorType || post.authorId != principal.id) {
            throw ForbiddenException("해당 게시글에 대한 권한이 없습니다")
        }
    }

    private fun toResponse(post: MarketplacePost): MarketplacePostResponse {
        val authorName = when (post.authorType) {
            MarketplaceAuthorType.CAFE_OWNER -> cafeOwnerRepository.findById(post.authorId)
                .map { it.name }.orElse("알 수 없음")
            MarketplaceAuthorType.ORGANIZER -> organizerRepository.findById(post.authorId)
                .map { it.name }.orElse("알 수 없음")
        }

        var cafeAddress: String? = null
        var cafeLatitude: Double? = null
        var cafeLongitude: Double? = null
        if (post.cafeId != null) {
            cafeRepository.findById(post.cafeId!!).ifPresent { cafe ->
                cafeAddress = cafe.address
                cafeLatitude = cafe.latitude
                cafeLongitude = cafe.longitude
            }
        }

        return MarketplacePostResponse(
            id = post.id,
            type = post.type,
            authorName = authorName,
            authorId = post.authorId,
            title = post.title,
            description = post.description,
            region = post.region,
            capacity = post.capacity,
            preferredDate = post.preferredDate,
            imageUrls = post.imageUrls,
            cafeId = post.cafeId,
            cafeAddress = cafeAddress,
            cafeLatitude = cafeLatitude,
            cafeLongitude = cafeLongitude,
            isActive = post.isActive
        )
    }
}
