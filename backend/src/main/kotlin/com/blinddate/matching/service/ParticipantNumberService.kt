package com.blinddate.matching.service

import com.blinddate.common.exception.NotFoundException
import com.blinddate.event.repository.BlindDateEventRepository
import com.blinddate.matching.entity.ParticipantNumber
import com.blinddate.matching.repository.ParticipantNumberRepository
import com.blinddate.member.entity.Gender
import com.blinddate.member.repository.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ParticipantNumberService(
    private val participantNumberRepository: ParticipantNumberRepository,
    private val eventRepository: BlindDateEventRepository,
    private val memberRepository: MemberRepository
) {

    @Transactional
    fun assignNumber(eventId: Long, memberId: Long, gender: Gender): ParticipantNumber {
        val event = eventRepository.findById(eventId)
            .orElseThrow { NotFoundException("이벤트를 찾을 수 없습니다") }

        val member = memberRepository.findById(memberId)
            .orElseThrow { NotFoundException("회원을 찾을 수 없습니다") }

        val maxNumber = participantNumberRepository.findMaxNumberByEventIdAndGender(eventId, gender) ?: 0
        val nextNumber = maxNumber + 1

        return participantNumberRepository.save(
            ParticipantNumber(
                event = event,
                member = member,
                number = nextNumber,
                gender = gender
            )
        )
    }
}
