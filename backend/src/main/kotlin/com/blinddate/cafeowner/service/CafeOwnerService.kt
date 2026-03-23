package com.blinddate.cafeowner.service

import com.blinddate.cafe.dto.CafeResponse
import com.blinddate.cafe.repository.CafeRepository
import com.blinddate.cafeowner.dto.UpdateCafeRequest
import com.blinddate.common.exception.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CafeOwnerService(
    private val cafeRepository: CafeRepository
) {
    fun getMyCafe(cafeId: Long): CafeResponse {
        val cafe = cafeRepository.findById(cafeId).orElseThrow { NotFoundException("카페를 찾을 수 없습니다") }
        return CafeResponse.from(cafe)
    }

    @Transactional
    fun updateMyCafe(cafeId: Long, request: UpdateCafeRequest): CafeResponse {
        val cafe = cafeRepository.findById(cafeId).orElseThrow { NotFoundException("카페를 찾을 수 없습니다") }
        cafe.apply {
            name = request.name; address = request.address; description = request.description
            logoUrl = request.logoUrl; coverImageUrl = request.coverImageUrl
        }
        return CafeResponse.from(cafe)
    }
}
