package com.blinddate.admin.service

import com.blinddate.admin.dto.*
import com.blinddate.cafe.dto.CafeResponse
import com.blinddate.cafe.entity.Cafe
import com.blinddate.cafe.repository.CafeRepository
import com.blinddate.cafeowner.entity.CafeOwner
import com.blinddate.cafeowner.repository.CafeOwnerRepository
import com.blinddate.common.exception.ConflictException
import com.blinddate.common.exception.NotFoundException
import com.blinddate.organizer.entity.Organizer
import com.blinddate.organizer.repository.OrganizerRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AdminService(
    private val cafeRepository: CafeRepository,
    private val cafeOwnerRepository: CafeOwnerRepository,
    private val organizerRepository: OrganizerRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @Transactional
    fun createCafe(request: CreateCafeRequest): CafeResponse {
        if (cafeRepository.existsBySlug(request.slug)) throw ConflictException("이미 사용 중인 slug입니다")
        val cafe = cafeRepository.save(Cafe(
            name = request.name, address = request.address, slug = request.slug,
            description = request.description, commissionRate = request.commissionRate
        ))
        return CafeResponse.from(cafe)
    }

    fun getCafes(): List<CafeResponse> = cafeRepository.findAll().map { CafeResponse.from(it) }

    @Transactional
    fun createCafeOwner(request: CreateCafeOwnerRequest) {
        val cafe = cafeRepository.findById(request.cafeId).orElseThrow { NotFoundException("카페를 찾을 수 없습니다") }
        cafeOwnerRepository.save(CafeOwner(
            cafe = cafe, name = request.name, phoneNumber = request.phoneNumber,
            email = request.email, password = passwordEncoder.encode(request.password),
            kakaoId = request.kakaoId
        ))
    }

    @Transactional
    fun createOrganizer(request: CreateOrganizerRequest): OrganizerResponse {
        val organizer = organizerRepository.save(Organizer(
            name = request.name, phoneNumber = request.phoneNumber,
            email = request.email, password = passwordEncoder.encode(request.password),
            description = request.description, commissionRate = request.commissionRate,
            kakaoId = request.kakaoId
        ))
        return organizer.toResponse()
    }

    fun getOrganizers(): List<OrganizerResponse> = organizerRepository.findAll().map { it.toResponse() }

    private fun Organizer.toResponse() = OrganizerResponse(
        id = id, name = name, phoneNumber = phoneNumber,
        email = email, description = description, commissionRate = commissionRate
    )
}
