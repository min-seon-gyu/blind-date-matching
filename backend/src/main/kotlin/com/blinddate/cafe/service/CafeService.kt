package com.blinddate.cafe.service

import com.blinddate.cafe.dto.CafeResponse
import com.blinddate.cafe.repository.CafeRepository
import com.blinddate.common.exception.NotFoundException
import org.springframework.stereotype.Service

@Service
class CafeService(private val cafeRepository: CafeRepository) {

    fun getBySlug(slug: String): CafeResponse {
        val cafe = cafeRepository.findBySlug(slug) ?: throw NotFoundException("카페를 찾을 수 없습니다")
        return CafeResponse.from(cafe)
    }
}
