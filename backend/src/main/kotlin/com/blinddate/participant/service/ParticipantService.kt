package com.blinddate.participant.service

import com.blinddate.common.exception.*
import com.blinddate.participant.dto.*
import com.blinddate.participant.entity.ParticipantProfile
import com.blinddate.participant.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ParticipantService(
    private val participantRepository: ParticipantRepository,
    private val profileRepository: ParticipantProfileRepository
) {
    fun getMe(participantId: Long): ParticipantResponse {
        val p = participantRepository.findById(participantId)
            .orElseThrow { NotFoundException("참가자를 찾을 수 없습니다") }
        return ParticipantResponse(p.id, p.nickname, p.phoneNumber, p.isProfileComplete)
    }

    @Transactional
    fun createProfile(participantId: Long, request: ProfileCreateRequest): ProfileResponse {
        val p = participantRepository.findById(participantId)
            .orElseThrow { NotFoundException("참가자를 찾을 수 없습니다") }
        if (profileRepository.existsByParticipantId(participantId)) throw ConflictException("프로필이 이미 존재합니다")

        val profile = profileRepository.save(ParticipantProfile(
            participant = p, name = request.name, age = request.age, gender = request.gender,
            job = request.job, height = request.height, mbti = request.mbti, hobby = request.hobby,
            drinking = request.drinking, smoking = request.smoking, religion = request.religion,
            idealType = request.idealType, introduction = request.introduction
        ))
        p.isProfileComplete = true
        return profile.toResponse()
    }

    fun getProfile(participantId: Long): ProfileResponse =
        profileRepository.findByParticipantId(participantId)
            .orElseThrow { NotFoundException("프로필을 찾을 수 없습니다") }.toResponse()

    @Transactional
    fun updateProfile(participantId: Long, request: ProfileCreateRequest): ProfileResponse {
        val profile = profileRepository.findByParticipantId(participantId)
            .orElseThrow { NotFoundException("프로필을 찾을 수 없습니다") }
        profile.apply {
            name = request.name; age = request.age; gender = request.gender; job = request.job
            height = request.height; mbti = request.mbti; hobby = request.hobby
            drinking = request.drinking; smoking = request.smoking; religion = request.religion
            idealType = request.idealType; introduction = request.introduction
        }
        return profile.toResponse()
    }

    private fun ParticipantProfile.toResponse() = ProfileResponse(
        id, name, age, gender, job, height, mbti, hobby, drinking, smoking, religion, idealType, introduction, photoUrl
    )
}
