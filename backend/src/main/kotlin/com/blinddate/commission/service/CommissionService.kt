package com.blinddate.commission.service

import com.blinddate.application.entity.ApplicationStatus
import com.blinddate.application.repository.ApplicationRepository
import com.blinddate.cafeowner.repository.CafeOwnerRepository
import com.blinddate.commission.dto.CommissionResponse
import com.blinddate.commission.entity.Commission
import com.blinddate.commission.entity.CommissionStatus
import com.blinddate.commission.entity.CommissionTargetType
import com.blinddate.commission.repository.CommissionRepository
import com.blinddate.common.exception.BadRequestException
import com.blinddate.common.exception.NotFoundException
import com.blinddate.event.repository.EventRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class CommissionService(
    private val commissionRepository: CommissionRepository,
    private val eventRepository: EventRepository,
    private val applicationRepository: ApplicationRepository,
    private val cafeOwnerRepository: CafeOwnerRepository
) {
    @Transactional
    fun createForEvent(eventId: Long): List<CommissionResponse> {
        val event = eventRepository.findById(eventId).orElseThrow { NotFoundException("이벤트를 찾을 수 없습니다") }
        if (commissionRepository.existsByEventId(eventId)) throw BadRequestException("이미 수수료가 생성된 이벤트입니다")

        val participantCount = applicationRepository.countByEventIdAndStatus(eventId, ApplicationStatus.APPROVED).toInt()
        val results = mutableListOf<CommissionResponse>()

        // Organizer commission
        val orgRate = event.organizer.commissionRate
        val orgUnitPrice = event.price * orgRate / 100
        val orgTotal = participantCount * orgUnitPrice
        val orgCommission = commissionRepository.save(Commission(
            cafe = event.cafe, event = event,
            targetType = CommissionTargetType.ORGANIZER, targetId = event.organizer.id,
            participantCount = participantCount, eventPrice = event.price,
            commissionRate = orgRate, unitPrice = orgUnitPrice, totalAmount = orgTotal
        ))
        results.add(orgCommission.toResponse())

        // Cafe owner commission
        val cafeRate = event.cafe.commissionRate
        val cafeUnitPrice = event.price * cafeRate / 100
        val cafeTotal = participantCount * cafeUnitPrice
        val cafeOwners = cafeOwnerRepository.findByCafeId(event.cafe.id)
        val cafeOwnerId = cafeOwners.firstOrNull()?.id ?: 0L
        val cafeCommission = commissionRepository.save(Commission(
            cafe = event.cafe, event = event,
            targetType = CommissionTargetType.CAFE_OWNER, targetId = cafeOwnerId,
            participantCount = participantCount, eventPrice = event.price,
            commissionRate = cafeRate, unitPrice = cafeUnitPrice, totalAmount = cafeTotal
        ))
        results.add(cafeCommission.toResponse())

        return results
    }

    @Transactional
    fun invoice(commissionId: Long): CommissionResponse {
        val commission = commissionRepository.findById(commissionId)
            .orElseThrow { NotFoundException("수수료 내역을 찾을 수 없습니다") }
        if (commission.status != CommissionStatus.PENDING) throw BadRequestException("대기 중인 수수료만 청구할 수 있습니다")
        commission.status = CommissionStatus.INVOICED
        commission.invoicedAt = LocalDateTime.now()
        return commission.toResponse()
    }

    @Transactional
    fun markPaid(commissionId: Long): CommissionResponse {
        val commission = commissionRepository.findById(commissionId)
            .orElseThrow { NotFoundException("수수료 내역을 찾을 수 없습니다") }
        if (commission.status != CommissionStatus.INVOICED) throw BadRequestException("청구된 수수료만 입금 처리할 수 있습니다")
        commission.status = CommissionStatus.PAID
        commission.paidAt = LocalDateTime.now()
        return commission.toResponse()
    }

    fun getByTargetType(targetType: CommissionTargetType, targetId: Long): List<CommissionResponse> =
        commissionRepository.findByTargetTypeAndTargetId(targetType, targetId).map { it.toResponse() }

    fun getByCafeId(cafeId: Long): List<CommissionResponse> =
        commissionRepository.findByCafeId(cafeId).map { it.toResponse() }

    fun getAll(): List<CommissionResponse> =
        commissionRepository.findAll().map { it.toResponse() }

    private fun Commission.toResponse() = CommissionResponse(
        id = id, cafeId = cafe.id, cafeName = cafe.name, eventId = event.id, eventTitle = event.title,
        targetType = targetType, targetId = targetId,
        participantCount = participantCount, eventPrice = eventPrice, commissionRate = commissionRate,
        unitPrice = unitPrice, totalAmount = totalAmount, status = status,
        invoicedAt = invoicedAt, paidAt = paidAt
    )
}
