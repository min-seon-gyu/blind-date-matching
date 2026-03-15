package com.blinddate.matching.service

import com.blinddate.application.entity.ApplicationStatus
import com.blinddate.application.repository.ApplicationRepository
import com.blinddate.common.exception.BadRequestException
import com.blinddate.common.exception.NotFoundException
import com.blinddate.event.repository.BlindDateEventRepository
import com.blinddate.matching.dto.MatchResultResponse
import com.blinddate.matching.dto.ParticipantNumberResponse
import com.blinddate.matching.entity.Choice
import com.blinddate.matching.entity.MatchResult
import com.blinddate.matching.repository.ChoiceRepository
import com.blinddate.matching.repository.MatchResultRepository
import com.blinddate.matching.repository.ParticipantNumberRepository
import com.blinddate.member.entity.Gender
import com.blinddate.member.repository.MemberProfileRepository
import com.blinddate.member.repository.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class MatchingService(
    private val participantNumberRepository: ParticipantNumberRepository,
    private val choiceRepository: ChoiceRepository,
    private val matchResultRepository: MatchResultRepository,
    private val eventRepository: BlindDateEventRepository,
    private val memberRepository: MemberRepository,
    private val memberProfileRepository: MemberProfileRepository,
    private val applicationRepository: ApplicationRepository
) {

    fun getParticipants(eventId: Long, memberId: Long): List<ParticipantNumberResponse> {
        val myProfile = memberProfileRepository.findByMemberId(memberId)
            .orElseThrow { NotFoundException("프로필을 찾을 수 없습니다") }

        val oppositeGender = if (myProfile.gender == Gender.MALE) Gender.FEMALE else Gender.MALE

        return participantNumberRepository.findByEventIdAndGender(eventId, oppositeGender)
            .map { pn ->
                ParticipantNumberResponse(
                    memberId = pn.member.id,
                    number = pn.number,
                    gender = pn.gender.name
                )
            }
    }

    @Transactional
    fun submitChoices(eventId: Long, memberId: Long, chosenMemberIds: List<Long>) {
        if (chosenMemberIds.size > 3) {
            throw BadRequestException("최대 3명까지만 선택할 수 있습니다")
        }

        // Validate chooser is an approved participant
        val application = applicationRepository.findByMemberIdAndEventId(memberId, eventId)
            .orElseThrow { NotFoundException("이벤트 참가 신청 내역을 찾을 수 없습니다") }

        if (application.status != ApplicationStatus.APPROVED) {
            throw BadRequestException("승인된 참가자만 선택할 수 있습니다")
        }

        val myProfile = memberProfileRepository.findByMemberId(memberId)
            .orElseThrow { NotFoundException("프로필을 찾을 수 없습니다") }

        val oppositeGender = if (myProfile.gender == Gender.MALE) Gender.FEMALE else Gender.MALE

        val event = eventRepository.findById(eventId)
            .orElseThrow { NotFoundException("이벤트를 찾을 수 없습니다") }

        val chooser = memberRepository.findById(memberId)
            .orElseThrow { NotFoundException("회원을 찾을 수 없습니다") }

        val choices = chosenMemberIds.map { chosenId ->
            val chosenProfile = memberProfileRepository.findByMemberId(chosenId)
                .orElseThrow { NotFoundException("선택된 회원의 프로필을 찾을 수 없습니다") }

            if (chosenProfile.gender != oppositeGender) {
                throw BadRequestException("이성 참가자만 선택할 수 있습니다")
            }

            val chosen = memberRepository.findById(chosenId)
                .orElseThrow { NotFoundException("선택된 회원을 찾을 수 없습니다") }

            Choice(event = event, chooser = chooser, chosen = chosen)
        }

        choiceRepository.saveAll(choices)
    }

    @Transactional
    fun processMatching(eventId: Long) {
        val choices = choiceRepository.findByEventId(eventId)

        // Build a map: chooserId -> set of chosenIds
        val choiceMap: Map<Long, Set<Long>> = choices
            .groupBy { it.chooser.id }
            .mapValues { (_, c) -> c.map { it.chosen.id }.toSet() }

        val createdPairs = mutableSetOf<Pair<Long, Long>>()

        choices.forEach { choice ->
            val chooserId = choice.chooser.id
            val chosenId = choice.chosen.id

            // Check if chosen also chose chooser (bidirectional)
            if (choiceMap[chosenId]?.contains(chooserId) == true) {
                // Determine male/female
                val chooserProfile = memberProfileRepository.findByMemberId(chooserId).orElse(null)
                val chosenProfile = memberProfileRepository.findByMemberId(chosenId).orElse(null)

                if (chooserProfile != null && chosenProfile != null) {
                    val maleId = if (chooserProfile.gender == Gender.MALE) chooserId else chosenId
                    val femaleId = if (chooserProfile.gender == Gender.FEMALE) chooserId else chosenId

                    val pair = Pair(maleId, femaleId)
                    if (!createdPairs.contains(pair) &&
                        !matchResultRepository.existsByEventIdAndMember1IdAndMember2Id(eventId, maleId, femaleId)
                    ) {
                        val event = eventRepository.findById(eventId)
                            .orElseThrow { NotFoundException("이벤트를 찾을 수 없습니다") }
                        val male = memberRepository.findById(maleId)
                            .orElseThrow { NotFoundException("회원을 찾을 수 없습니다") }
                        val female = memberRepository.findById(femaleId)
                            .orElseThrow { NotFoundException("회원을 찾을 수 없습니다") }

                        matchResultRepository.save(
                            MatchResult(event = event, member1 = male, member2 = female)
                        )
                        createdPairs.add(pair)
                    }
                }
            }
        }
    }

    fun getMatchResult(eventId: Long, memberId: Long): List<MatchResultResponse> {
        val results = matchResultRepository.findByEventIdAndMember1IdOrEventIdAndMember2Id(
            eventId, memberId, eventId, memberId
        )

        return results.map { result ->
            val matchedMember = if (result.member1.id == memberId) result.member2 else result.member1
            MatchResultResponse(
                matchResultId = result.id,
                eventId = result.event.id,
                matchedMemberId = matchedMember.id,
                matchedMemberNickname = matchedMember.nickname,
                notified = result.notified
            )
        }
    }
}
