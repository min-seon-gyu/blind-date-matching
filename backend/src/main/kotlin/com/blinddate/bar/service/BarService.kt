package com.blinddate.bar.service
import com.blinddate.bar.dto.BarResponse
import com.blinddate.bar.entity.Bar
import com.blinddate.bar.repository.BarRepository
import com.blinddate.common.exception.NotFoundException
import org.springframework.stereotype.Service
@Service
class BarService(private val barRepository: BarRepository) {
    fun getBySlug(slug: String): BarResponse {
        val bar = barRepository.findBySlug(slug).orElseThrow { NotFoundException("바를 찾을 수 없습니다") }
        return bar.toResponse()
    }
    private fun Bar.toResponse() = BarResponse(id, name, address, description, logoUrl, coverImageUrl, slug, isActive)
}
