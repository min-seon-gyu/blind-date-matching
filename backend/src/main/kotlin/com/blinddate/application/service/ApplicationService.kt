package com.blinddate.application.service

import com.blinddate.application.dto.ApplicationResponse
import com.blinddate.application.entity.Application
import com.blinddate.application.entity.ApplicationStatus
import com.blinddate.application.repository.ApplicationRepository
import com.blinddate.barowner.repository.BarOwnerRepository
import com.blinddate.common.exception.*
import com.blinddate.event.entity.EventStatus
import com.blinddate.event.repository.EventRepository
import com.blinddate.participant.entity.Gender
import com.blinddate.participant.repository.ParticipantProfileRepository
import com.blinddate.participant.repository.ParticipantRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ApplicationService(
    private val applicationRepository: ApplicationRepository,
    private val eventRepository: EventRepository,
    private val participantRepository: ParticipantRepository,
    private val profileRepository: ParticipantProfileRepository,
    private val barOwnerRepository: BarOwnerRepository
) {
    @Transactional
    fun apply(participantId: Long, eventId: Long): ApplicationResponse {
        val participant = participantRepository.findById(participantId)
            .orElseThrow { NotFoundException("참가자를 찾을 수 없습니다") }
        val event = eventRepository.findById(eventId)
            .orElseThrow { NotFoundException("이벤트를 찾을 수 없습니다") }

        if (event.isDeleted()) throw NotFoundException("삭제된 이벤트입니다")
        if (event.status != EventStatus.OPEN) throw BadRequestException("신청 가능한 이벤트가 아닙니다")

        val profile = profileRepository.findByParticipantId(participantId)
            .orElseThrow { BadRequestException("프로필을 먼저 작성해야 합니다") }

        event.minAge?.let { min -> if (profile.age < min) throw BadRequestException("나이 조건을 충족하지 못합니다 (최소 ${min}세)") }
        event.maxAge?.let { max -> if (profile.age > max) throw BadRequestException("나이 조건을 충족하지 못합니다 (최대 ${max}세)") }

        if (applicationRepository.findByParticipantIdAndEventId(participantId, eventId).isPresent) {
            throw ConflictException("이미 신청한 이벤트입니다")
        }

        val application = applicationRepository.save(Application(participant = participant, event = event))
        return application.toResponse()
    }

    @Transactional
    fun cancel(participantId: Long, applicationId: Long) {
        val application = applicationRepository.findById(applicationId)
            .orElseThrow { NotFoundException("신청 내역을 찾을 수 없습니다") }
        if (application.participant.id != participantId) throw ForbiddenException("본인의 신청만 취소할 수 있습니다")
        if (application.status != ApplicationStatus.PENDING) throw BadRequestException("대기 중인 신청만 취소할 수 있습니다")
        application.status = ApplicationStatus.CANCELLED
    }

    fun getMyApplications(participantId: Long): List<ApplicationResponse> =
        applicationRepository.findByParticipantId(participantId).map { it.toResponse() }

    @Transactional
    fun approve(barOwnerId: Long, barId: Long, applicationId: Long): ApplicationResponse {
        val application = applicationRepository.findById(applicationId)
            .orElseThrow { NotFoundException("신청 내역을 찾을 수 없습니다") }

        if (application.event.bar.id != barId) throw ForbiddenException("해당 신청에 대한 권한이 없습니다")
        if (application.status != ApplicationStatus.PENDING) throw BadRequestException("대기 중인 신청만 승인할 수 있습니다")

        val profile = profileRepository.findByParticipantId(application.participant.id)
            .orElseThrow { BadRequestException("신청자의 프로필이 없습니다") }

        val event = application.event
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

        val barOwner = barOwnerRepository.findById(barOwnerId)
            .orElseThrow { NotFoundException("바 사장님을 찾을 수 없습니다") }

        application.status = ApplicationStatus.APPROVED
        application.reviewedAt = java.time.LocalDateTime.now()
        application.reviewedBy = barOwner
        return application.toResponse()
    }

    @Transactional
    fun reject(barOwnerId: Long, barId: Long, applicationId: Long, reason: String): ApplicationResponse {
        val application = applicationRepository.findById(applicationId)
            .orElseThrow { NotFoundException("신청 내역을 찾을 수 없습니다") }

        if (application.event.bar.id != barId) throw ForbiddenException("해당 신청에 대한 권한이 없습니다")
        if (application.status != ApplicationStatus.PENDING) throw BadRequestException("대기 중인 신청만 거절할 수 있습니다")

        val barOwner = barOwnerRepository.findById(barOwnerId)
            .orElseThrow { NotFoundException("바 사장님을 찾을 수 없습니다") }

        application.status = ApplicationStatus.REJECTED
        application.reviewedAt = java.time.LocalDateTime.now()
        application.reviewedBy = barOwner
        application.rejectReason = reason
        return application.toResponse()
    }

    fun getApplicationsByEvent(barId: Long, eventId: Long): List<ApplicationResponse> {
        val event = eventRepository.findById(eventId).orElseThrow { NotFoundException("이벤트를 찾을 수 없습니다") }
        if (event.bar.id != barId) throw ForbiddenException("해당 이벤트에 대한 권한이 없습니다")
        return applicationRepository.findByEventId(eventId).map { it.toResponse() }
    }

    private fun Application.toResponse() = ApplicationResponse(
        id = id, eventId = event.id, eventTitle = event.title,
        participantId = participant.id, participantNickname = participant.nickname,
        status = status, appliedAt = appliedAt, reviewedAt = reviewedAt, rejectReason = rejectReason
    )
}
