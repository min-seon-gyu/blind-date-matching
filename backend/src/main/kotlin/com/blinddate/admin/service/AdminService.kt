package com.blinddate.admin.service

import com.blinddate.admin.dto.*
import com.blinddate.bar.dto.BarResponse
import com.blinddate.bar.entity.Bar
import com.blinddate.bar.repository.BarRepository
import com.blinddate.barowner.entity.BarOwner
import com.blinddate.barowner.repository.BarOwnerRepository
import com.blinddate.common.exception.ConflictException
import com.blinddate.common.exception.NotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AdminService(
    private val barRepository: BarRepository,
    private val barOwnerRepository: BarOwnerRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @Transactional
    fun createBar(request: CreateBarRequest): BarResponse {
        if (barRepository.existsBySlug(request.slug)) throw ConflictException("이미 사용 중인 slug입니다")
        val bar = barRepository.save(Bar(
            name = request.name, address = request.address, slug = request.slug,
            description = request.description, commissionRate = request.commissionRate
        ))
        return bar.toResponse()
    }

    fun getBars(): List<BarResponse> = barRepository.findAll().map { it.toResponse() }

    @Transactional
    fun createBarOwner(request: CreateBarOwnerRequest) {
        val bar = barRepository.findById(request.barId).orElseThrow { NotFoundException("바를 찾을 수 없습니다") }
        barOwnerRepository.save(BarOwner(
            bar = bar, name = request.name, phoneNumber = request.phoneNumber,
            email = request.email, password = passwordEncoder.encode(request.password),
            kakaoId = request.kakaoId
        ))
    }

    private fun Bar.toResponse() = BarResponse(id, name, address, description, logoUrl, coverImageUrl, slug, isActive)
}
