package com.blinddate.partnership.service

import com.blinddate.auth.jwt.UserPrincipal
import com.blinddate.auth.jwt.UserType
import com.blinddate.cafe.repository.CafeRepository
import com.blinddate.common.exception.BadRequestException
import com.blinddate.common.exception.ForbiddenException
import com.blinddate.common.exception.NotFoundException
import com.blinddate.marketplace.entity.MarketplacePost
import com.blinddate.marketplace.entity.MarketplacePostType
import com.blinddate.organizer.repository.OrganizerRepository
import com.blinddate.partnership.dto.PartnershipResponse
import com.blinddate.partnership.dto.toResponse
import com.blinddate.partnership.entity.Partnership
import com.blinddate.partnership.entity.PartnershipRequester
import com.blinddate.partnership.entity.PartnershipStatus
import com.blinddate.partnership.repository.PartnershipRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class PartnershipService(
    private val partnershipRepository: PartnershipRepository,
    private val cafeRepository: CafeRepository,
    private val organizerRepository: OrganizerRepository
) {
    @Transactional
    fun requestPartnership(principal: UserPrincipal, post: MarketplacePost, message: String?): PartnershipResponse {
        val cafeId: Long
        val organizerId: Long
        val requestedBy: PartnershipRequester

        when {
            principal.userType == UserType.ORGANIZER && post.type == MarketplacePostType.OFFER_SPACE -> {
                cafeId = post.cafeId ?: throw BadRequestException("해당 게시글에 카페 정보가 없습니다")
                organizerId = principal.id
                requestedBy = PartnershipRequester.ORGANIZER
                if (post.authorId == principal.id) throw BadRequestException("자신의 게시글에 제휴를 요청할 수 없습니다")
            }
            principal.userType == UserType.CAFE_OWNER && post.type == MarketplacePostType.SEEK_SPACE -> {
                cafeId = principal.cafeId ?: throw BadRequestException("카페 정보가 없습니다")
                organizerId = post.authorId
                requestedBy = PartnershipRequester.CAFE_OWNER
                if (post.authorId == principal.id) throw BadRequestException("자신의 게시글에 제휴를 요청할 수 없습니다")
            }
            else -> throw BadRequestException("해당 게시글에 제휴를 요청할 수 없습니다")
        }

        val existing = partnershipRepository.findByCafeIdAndOrganizerIdAndStatusIn(
            cafeId, organizerId, listOf(PartnershipStatus.ACTIVE, PartnershipStatus.PENDING)
        )
        if (existing != null) throw BadRequestException("이미 제휴 요청이 존재합니다")

        val cafe = cafeRepository.findById(cafeId).orElseThrow { NotFoundException("카페를 찾을 수 없습니다") }
        val organizer = organizerRepository.findById(organizerId).orElseThrow { NotFoundException("주최자를 찾을 수 없습니다") }

        val partnership = partnershipRepository.save(
            Partnership(
                cafe = cafe,
                organizer = organizer,
                requestedBy = requestedBy,
                message = message
            )
        )
        return partnership.toResponse()
    }

    @Transactional
    fun accept(partnershipId: Long, actorId: Long): PartnershipResponse {
        val partnership = findPartnership(partnershipId)
        validateResponder(partnership, actorId)
        if (partnership.status != PartnershipStatus.PENDING) throw BadRequestException("대기 중인 제휴만 수락할 수 있습니다")
        partnership.status = PartnershipStatus.ACTIVE
        partnership.respondedAt = LocalDateTime.now()
        return partnership.toResponse()
    }

    @Transactional
    fun reject(partnershipId: Long, actorId: Long): PartnershipResponse {
        val partnership = findPartnership(partnershipId)
        validateResponder(partnership, actorId)
        if (partnership.status != PartnershipStatus.PENDING) throw BadRequestException("대기 중인 제휴만 거절할 수 있습니다")
        partnership.status = PartnershipStatus.TERMINATED
        partnership.respondedAt = LocalDateTime.now()
        partnership.terminatedAt = LocalDateTime.now()
        return partnership.toResponse()
    }

    @Transactional
    fun terminate(partnershipId: Long, actorId: Long): PartnershipResponse {
        val partnership = findPartnership(partnershipId)
        if (partnership.cafe.id != actorId && partnership.organizer.id != actorId) {
            throw ForbiddenException("해당 제휴에 대한 권한이 없습니다")
        }
        if (partnership.status != PartnershipStatus.ACTIVE) throw BadRequestException("활성 상태인 제휴만 종료할 수 있습니다")
        partnership.status = PartnershipStatus.TERMINATED
        partnership.terminatedAt = LocalDateTime.now()
        return partnership.toResponse()
    }

    fun getByCafeId(cafeId: Long): List<PartnershipResponse> =
        partnershipRepository.findByCafeId(cafeId).map { it.toResponse() }

    fun getByOrganizerId(organizerId: Long): List<PartnershipResponse> =
        partnershipRepository.findByOrganizerId(organizerId).map { it.toResponse() }

    fun getAll(): List<PartnershipResponse> =
        partnershipRepository.findAll().map { it.toResponse() }

    fun hasActivePartnership(cafeId: Long, organizerId: Long): Boolean =
        partnershipRepository.findByCafeIdAndOrganizerIdAndStatusIn(
            cafeId, organizerId, listOf(PartnershipStatus.ACTIVE)
        ) != null

    private fun findPartnership(id: Long): Partnership =
        partnershipRepository.findById(id).orElseThrow { NotFoundException("제휴를 찾을 수 없습니다") }

    private fun validateResponder(partnership: Partnership, actorId: Long) {
        // The responder is the opposite side of who requested
        val isResponder = when (partnership.requestedBy) {
            PartnershipRequester.ORGANIZER -> partnership.cafe.id == actorId
            PartnershipRequester.CAFE_OWNER -> partnership.organizer.id == actorId
        }
        if (!isResponder) throw ForbiddenException("해당 제휴에 대한 권한이 없습니다")
    }
}
