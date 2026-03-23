package com.blinddate.barowner.service

import com.blinddate.bar.dto.BarResponse
import com.blinddate.bar.entity.Bar
import com.blinddate.bar.repository.BarRepository
import com.blinddate.barowner.dto.BarUpdateRequest
import com.blinddate.common.exception.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Deprecated: replaced by CafeOwnerService and OrganizerService.
 * Kept for backward compatibility with old bar-owner endpoints.
 */
@Service
@Transactional(readOnly = true)
class BarOwnerService(
    private val barRepository: BarRepository
) {
    fun getMyBar(barId: Long): BarResponse {
        val bar = barRepository.findById(barId).orElseThrow { NotFoundException("바를 찾을 수 없습니다") }
        return bar.toBarResponse()
    }

    @Transactional
    fun updateMyBar(barId: Long, request: BarUpdateRequest): BarResponse {
        val bar = barRepository.findById(barId).orElseThrow { NotFoundException("바를 찾을 수 없습니다") }
        bar.apply {
            name = request.name; address = request.address; description = request.description
            logoUrl = request.logoUrl; coverImageUrl = request.coverImageUrl
        }
        return bar.toBarResponse()
    }

    private fun Bar.toBarResponse() = BarResponse(id, name, address, description, logoUrl, coverImageUrl, slug, isActive)
}
