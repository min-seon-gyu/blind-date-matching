package com.blinddate.member.service

import com.blinddate.common.exception.ConflictException
import com.blinddate.common.exception.NotFoundException
import com.blinddate.member.dto.*
import com.blinddate.member.entity.MemberProfile
import com.blinddate.member.repository.MemberProfileRepository
import com.blinddate.member.repository.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class MemberService(
    private val memberRepository: MemberRepository,
    private val profileRepository: MemberProfileRepository
) {
    fun getMe(memberId: Long): MemberResponse {
        val member = memberRepository.findById(memberId).orElseThrow { NotFoundException("회원을 찾을 수 없습니다") }
        val hasProfile = profileRepository.existsByMemberId(memberId)
        return MemberResponse(member.id, member.email, member.nickname, member.phoneNumber, member.role, hasProfile)
    }

    @Transactional
    fun createProfile(memberId: Long, request: ProfileCreateRequest): ProfileResponse {
        val member = memberRepository.findById(memberId).orElseThrow { NotFoundException("회원을 찾을 수 없습니다") }
        if (profileRepository.existsByMemberId(memberId)) throw ConflictException("프로필이 이미 존재합니다")
        val profile = profileRepository.save(MemberProfile(
            member = member, name = request.name, age = request.age, gender = request.gender,
            job = request.job, height = request.height, mbti = request.mbti, hobby = request.hobby,
            drinking = request.drinking, smoking = request.smoking, religion = request.religion,
            idealType = request.idealType, introduction = request.introduction
        ))
        return profile.toResponse()
    }

    fun getProfile(memberId: Long): ProfileResponse {
        return profileRepository.findByMemberId(memberId)
            .orElseThrow { NotFoundException("프로필을 찾을 수 없습니다") }.toResponse()
    }

    @Transactional
    fun updateProfile(memberId: Long, request: ProfileCreateRequest): ProfileResponse {
        val profile = profileRepository.findByMemberId(memberId)
            .orElseThrow { NotFoundException("프로필을 찾을 수 없습니다") }
        profile.apply {
            name = request.name; age = request.age; gender = request.gender; job = request.job
            height = request.height; mbti = request.mbti; hobby = request.hobby
            drinking = request.drinking; smoking = request.smoking; religion = request.religion
            idealType = request.idealType; introduction = request.introduction
        }
        return profile.toResponse()
    }

    private fun MemberProfile.toResponse() = ProfileResponse(
        id, name, age, gender, job, height, mbti, hobby, drinking, smoking, religion, idealType, introduction, photoUrl
    )
}
