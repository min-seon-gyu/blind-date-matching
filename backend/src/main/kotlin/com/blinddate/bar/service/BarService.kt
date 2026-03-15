package com.blinddate.bar.service

import com.blinddate.bar.dto.*
import com.blinddate.bar.entity.BarReservation
import com.blinddate.bar.entity.BarReservationStatus
import com.blinddate.bar.entity.BarVisitLog
import com.blinddate.bar.repository.BarRepository
import com.blinddate.bar.repository.BarReservationRepository
import com.blinddate.bar.repository.BarVisitLogRepository
import com.blinddate.common.exception.BadRequestException
import com.blinddate.common.exception.ForbiddenException
import com.blinddate.common.exception.NotFoundException
import com.blinddate.member.repository.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class BarService(
    private val barRepository: BarRepository,
    private val barReservationRepository: BarReservationRepository,
    private val barVisitLogRepository: BarVisitLogRepository,
    private val memberRepository: MemberRepository,
    private val barStatusRedisService: BarStatusRedisService
) {

    fun getStatus(barId: Long): BarStatusResponse {
        val bar = barRepository.findById(barId)
            .orElseThrow { NotFoundException("바를 찾을 수 없습니다") }
        val status = barStatusRedisService.getStatus(barId)
        return BarStatusResponse(
            id = bar.id,
            name = bar.name,
            address = bar.address,
            totalSeats = bar.totalSeats,
            currentMaleCount = status.maleCount,
            currentFemaleCount = status.femaleCount,
            remainingSeats = bar.totalSeats - status.maleCount - status.femaleCount,
            isOpen = bar.isOpen,
            openTime = bar.openTime,
            closeTime = bar.closeTime
        )
    }

    @Transactional
    fun reserve(memberId: Long, barId: Long, request: BarReserveRequest): BarReservationResponse {
        val bar = barRepository.findById(barId)
            .orElseThrow { NotFoundException("바를 찾을 수 없습니다") }

        if (!bar.isOpen) throw BadRequestException("현재 영업 중인 바가 아닙니다")

        val member = memberRepository.findById(memberId)
            .orElseThrow { NotFoundException("회원을 찾을 수 없습니다") }

        val reservation = barReservationRepository.save(
            BarReservation(
                bar = bar,
                member = member,
                date = request.date,
                time = request.time,
                status = BarReservationStatus.CONFIRMED
            )
        )

        return reservation.toResponse()
    }

    @Transactional
    fun cancelReservation(memberId: Long, reservationId: Long) {
        val reservation = barReservationRepository.findById(reservationId)
            .orElseThrow { NotFoundException("예약을 찾을 수 없습니다") }

        if (reservation.member.id != memberId) throw ForbiddenException("본인의 예약만 취소할 수 있습니다")
        if (reservation.status != BarReservationStatus.CONFIRMED) throw BadRequestException("취소 가능한 예약이 아닙니다")

        reservation.status = BarReservationStatus.CANCELLED
    }

    fun getMyReservations(memberId: Long): List<BarReservationResponse> =
        barReservationRepository.findByMemberIdOrderByDateDesc(memberId).map { it.toResponse() }

    @Transactional
    fun openBar(barId: Long) {
        val bar = barRepository.findById(barId)
            .orElseThrow { NotFoundException("바를 찾을 수 없습니다") }
        bar.isOpen = true
        barStatusRedisService.resetCounts(barId)
    }

    @Transactional
    fun closeBar(barId: Long) {
        val bar = barRepository.findById(barId)
            .orElseThrow { NotFoundException("바를 찾을 수 없습니다") }
        bar.isOpen = false

        // Check out all current visitors
        val currentVisitors = barVisitLogRepository.findByBarIdAndCheckOutAtIsNull(barId)
        val now = LocalDateTime.now()
        currentVisitors.forEach { log ->
            log.checkOutAt = now
        }

        // Reset DB counts and Redis counts
        bar.currentMaleCount = 0
        bar.currentFemaleCount = 0
        barStatusRedisService.resetCounts(barId)
    }

    @Transactional
    fun checkIn(barId: Long, request: CheckInRequest): BarVisitLog {
        val bar = barRepository.findById(barId)
            .orElseThrow { NotFoundException("바를 찾을 수 없습니다") }

        val member = request.memberId?.let {
            memberRepository.findById(it).orElseThrow { NotFoundException("회원을 찾을 수 없습니다") }
        }

        val visitLog = barVisitLogRepository.save(
            BarVisitLog(
                bar = bar,
                member = member,
                gender = request.gender,
                checkInAt = LocalDateTime.now()
            )
        )

        // Increment Redis count
        barStatusRedisService.incrementCount(barId, request.gender)

        // Increment DB count
        when (request.gender) {
            com.blinddate.member.entity.Gender.MALE -> bar.currentMaleCount++
            com.blinddate.member.entity.Gender.FEMALE -> bar.currentFemaleCount++
        }

        return visitLog
    }

    @Transactional
    fun checkOut(visitLogId: Long) {
        val visitLog = barVisitLogRepository.findById(visitLogId)
            .orElseThrow { NotFoundException("방문 기록을 찾을 수 없습니다") }

        if (visitLog.checkOutAt != null) throw BadRequestException("이미 체크아웃된 방문자입니다")

        visitLog.checkOutAt = LocalDateTime.now()

        val barId = visitLog.bar.id
        barStatusRedisService.decrementCount(barId, visitLog.gender)

        val bar = visitLog.bar
        when (visitLog.gender) {
            com.blinddate.member.entity.Gender.MALE -> {
                if (bar.currentMaleCount > 0) bar.currentMaleCount--
            }
            com.blinddate.member.entity.Gender.FEMALE -> {
                if (bar.currentFemaleCount > 0) bar.currentFemaleCount--
            }
        }
    }

    fun getCurrentVisitors(barId: Long): List<VisitorResponse> {
        return barVisitLogRepository.findByBarIdAndCheckOutAtIsNull(barId).map { log ->
            VisitorResponse(
                visitLogId = log.id,
                gender = log.gender,
                memberId = log.member?.id,
                memberName = log.member?.nickname,
                checkInAt = log.checkInAt
            )
        }
    }

    private fun BarReservation.toResponse() = BarReservationResponse(
        id = id,
        barId = bar.id,
        date = date,
        time = time,
        status = status,
        createdAt = createdAt
    )
}
