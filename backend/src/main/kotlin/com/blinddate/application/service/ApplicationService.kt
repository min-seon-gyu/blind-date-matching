package com.blinddate.application.service

import com.blinddate.application.dto.ApplicationDetailResponse
import com.blinddate.application.dto.ApplicationResponse
import com.blinddate.application.entity.Application
import com.blinddate.application.entity.ApplicationStatus
import com.blinddate.application.repository.ApplicationRepository
import com.blinddate.common.exception.BadRequestException
import com.blinddate.common.exception.ConflictException
import com.blinddate.common.exception.ForbiddenException
import com.blinddate.common.exception.NotFoundException
import com.blinddate.event.entity.EventStatus
import com.blinddate.event.repository.BlindDateEventRepository
import com.blinddate.member.entity.Gender
import com.blinddate.member.repository.MemberProfileRepository
import com.blinddate.member.repository.MemberRepository
import com.blinddate.payment.service.PaymentService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class ApplicationService(
    private val applicationRepository: ApplicationRepository,
    private val eventRepository: BlindDateEventRepository,
    private val memberRepository: MemberRepository,
    private val memberProfileRepository: MemberProfileRepository,
    private val paymentService: PaymentService
) {

    @Transactional
    fun apply(memberId: Long, eventId: Long): ApplicationResponse {
        val member = memberRepository.findById(memberId)
            .orElseThrow { NotFoundException("회원을 찾을 수 없습니다") }

        val event = eventRepository.findById(eventId)
            .orElseThrow { NotFoundException("이벤트를 찾을 수 없습니다") }

        if (event.isDeleted()) throw NotFoundException("삭제된 이벤트입니다")
        if (event.status != EventStatus.OPEN) throw BadRequestException("신청 가능한 이벤트가 아닙니다")

        // Profile is always required to apply
        val profile = memberProfileRepository.findByMemberId(memberId)
            .orElseThrow { BadRequestException("프로필을 먼저 작성해야 합니다") }

        // Age constraint check
        event.minAge?.let { min ->
            if (profile.age < min) throw BadRequestException("나이 조건을 충족하지 못합니다 (최소 ${min}세)")
        }
        event.maxAge?.let { max ->
            if (profile.age > max) throw BadRequestException("나이 조건을 충족하지 못합니다 (최대 ${max}세)")
        }

        if (applicationRepository.findByMemberIdAndEventId(memberId, eventId).isPresent) {
            throw ConflictException("이미 신청한 이벤트입니다")
        }

        val application = applicationRepository.save(
            Application(member = member, event = event)
        )
        return application.toResponse()
    }

    @Transactional
    fun cancel(memberId: Long, applicationId: Long) {
        val application = applicationRepository.findById(applicationId)
            .orElseThrow { NotFoundException("신청 내역을 찾을 수 없습니다") }

        if (application.member.id != memberId) throw ForbiddenException("본인의 신청만 취소할 수 있습니다")
        if (application.status != ApplicationStatus.PAID) throw BadRequestException("결제 완료 상태인 신청만 취소할 수 있습니다")

        application.status = ApplicationStatus.CANCELLED
        paymentService.refund(applicationId)
    }

    @Transactional
    fun approve(adminId: Long, applicationId: Long): ApplicationResponse {
        val admin = memberRepository.findById(adminId)
            .orElseThrow { NotFoundException("관리자를 찾을 수 없습니다") }

        val application = applicationRepository.findById(applicationId)
            .orElseThrow { NotFoundException("신청 내역을 찾을 수 없습니다") }

        if (application.status != ApplicationStatus.PAID) {
            throw BadRequestException("결제 완료 상태인 신청만 승인할 수 있습니다")
        }

        val profile = memberProfileRepository.findByMemberId(application.member.id)
            .orElseThrow { BadRequestException("신청자의 프로필이 없습니다") }

        // Pessimistic lock on event to safely increment counts
        val event = eventRepository.findByIdForUpdate(application.event.id)
            .orElseThrow { NotFoundException("이벤트를 찾을 수 없습니다") }

        when (profile.gender) {
            Gender.MALE -> {
                if (!event.hasAvailableMaleSlots()) throw BadRequestException("남성 정원이 초과되었습니다")
                event.currentMaleCount++
            }
            Gender.FEMALE -> {
                if (!event.hasAvailableFemaleSlots()) throw BadRequestException("여성 정원이 초과되었습니다")
                event.currentFemaleCount++
            }
        }

        application.status = ApplicationStatus.APPROVED
        application.reviewedAt = LocalDateTime.now()
        application.reviewedBy = admin

        return application.toResponse()
    }

    @Transactional
    fun reject(adminId: Long, applicationId: Long, reason: String): ApplicationResponse {
        val admin = memberRepository.findById(adminId)
            .orElseThrow { NotFoundException("관리자를 찾을 수 없습니다") }

        val application = applicationRepository.findById(applicationId)
            .orElseThrow { NotFoundException("신청 내역을 찾을 수 없습니다") }

        if (application.status != ApplicationStatus.PAID) {
            throw BadRequestException("결제 완료 상태인 신청만 거절할 수 있습니다")
        }

        application.status = ApplicationStatus.REJECTED
        application.reviewedAt = LocalDateTime.now()
        application.reviewedBy = admin
        application.rejectReason = reason
        paymentService.refund(applicationId)

        return application.toResponse()
    }

    fun getMyApplications(memberId: Long): List<ApplicationResponse> =
        applicationRepository.findByMemberId(memberId).map { it.toResponse() }

    fun getApplicationDetail(applicationId: Long): ApplicationDetailResponse {
        val application = applicationRepository.findById(applicationId)
            .orElseThrow { NotFoundException("신청 내역을 찾을 수 없습니다") }

        val profile = memberProfileRepository.findByMemberId(application.member.id).orElse(null)

        return ApplicationDetailResponse(
            id = application.id,
            eventId = application.event.id,
            eventTitle = application.event.title,
            memberId = application.member.id,
            memberNickname = application.member.nickname,
            memberName = profile?.name,
            memberAge = profile?.age,
            memberGender = profile?.gender?.name,
            memberJob = profile?.job,
            memberPhotoUrl = profile?.photoUrl,
            status = application.status,
            appliedAt = application.appliedAt,
            reviewedAt = application.reviewedAt,
            rejectReason = application.rejectReason
        )
    }

    fun getAllApplications(): List<ApplicationResponse> =
        applicationRepository.findAll().map { it.toResponse() }

    private fun Application.toResponse() = ApplicationResponse(
        id = id,
        eventId = event.id,
        eventTitle = event.title,
        memberId = member.id,
        memberNickname = member.nickname,
        status = status,
        appliedAt = appliedAt,
        reviewedAt = reviewedAt,
        rejectReason = rejectReason
    )
}
