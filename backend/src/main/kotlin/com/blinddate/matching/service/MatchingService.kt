package com.blinddate.matching.service

import com.blinddate.application.entity.ApplicationStatus
import com.blinddate.application.repository.ApplicationRepository
import com.blinddate.common.exception.BadRequestException
import com.blinddate.common.exception.NotFoundException
import com.blinddate.event.repository.EventRepository
import com.blinddate.matching.dto.*
import com.blinddate.matching.entity.Choice
import com.blinddate.matching.entity.MatchResult
import com.blinddate.matching.repository.ChoiceRepository
import com.blinddate.matching.repository.MatchResultRepository
import com.blinddate.matching.repository.ParticipantNumberRepository
import com.blinddate.participant.entity.Gender
import com.blinddate.participant.repository.ParticipantProfileRepository
import com.blinddate.participant.repository.ParticipantRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class MatchingService(
    private val participantNumberRepository: ParticipantNumberRepository,
    private val choiceRepository: ChoiceRepository,
    private val matchResultRepository: MatchResultRepository,
    private val eventRepository: EventRepository,
    private val participantRepository: ParticipantRepository,
    private val profileRepository: ParticipantProfileRepository,
    private val applicationRepository: ApplicationRepository
) {
    fun getParticipants(eventId: Long, participantId: Long): List<ParticipantInfoResponse> {
        val myProfile = profileRepository.findByParticipantId(participantId)
            .orElseThrow { NotFoundException("프로필을 찾을 수 없습니다") }
        val oppositeGender = if (myProfile.gender == Gender.MALE) Gender.FEMALE else Gender.MALE

        return participantNumberRepository.findByEventIdAndGender(eventId, oppositeGender).map { pn ->
            val profile = profileRepository.findByParticipantId(pn.participant.id).orElse(null)
            ParticipantInfoResponse(
                participantId = pn.participant.id,
                number = pn.number,
                gender = pn.gender.name,
                age = profile?.age ?: 0,
                job = profile?.job ?: "",
                introduction = profile?.introduction ?: ""
            )
        }
    }

    @Transactional
    fun submitChoices(eventId: Long, participantId: Long, chosenIds: List<Long>) {
        val event = eventRepository.findById(eventId).orElseThrow { NotFoundException("이벤트를 찾을 수 없습니다") }

        if (chosenIds.size > event.maxChoices) {
            throw BadRequestException("최대 ${event.maxChoices}명까지만 선택할 수 있습니다")
        }

        if (event.choiceDeadline != null && LocalDateTime.now().isAfter(event.choiceDeadline)) {
            throw BadRequestException("선택 마감 시간이 지났습니다")
        }

        val application = applicationRepository.findByParticipantIdAndEventId(participantId, eventId)
            .orElseThrow { NotFoundException("이벤트 참가 신청 내역을 찾을 수 없습니다") }
        if (application.status != ApplicationStatus.APPROVED) {
            throw BadRequestException("승인된 참가자만 선택할 수 있습니다")
        }

        val myProfile = profileRepository.findByParticipantId(participantId)
            .orElseThrow { NotFoundException("프로필을 찾을 수 없습니다") }
        val oppositeGender = if (myProfile.gender == Gender.MALE) Gender.FEMALE else Gender.MALE

        val chooser = participantRepository.findById(participantId)
            .orElseThrow { NotFoundException("참가자를 찾을 수 없습니다") }

        val choices = chosenIds.map { chosenId ->
            val chosenProfile = profileRepository.findByParticipantId(chosenId)
                .orElseThrow { NotFoundException("선택된 참가자의 프로필을 찾을 수 없습니다") }
            if (chosenProfile.gender != oppositeGender) {
                throw BadRequestException("이성 참가자만 선택할 수 있습니다")
            }
            val chosen = participantRepository.findById(chosenId)
                .orElseThrow { NotFoundException("선택된 참가자를 찾을 수 없습니다") }
            Choice(event = event, chooser = chooser, chosen = chosen)
        }

        // Delete existing choices and save new ones
        choiceRepository.deleteByEventIdAndChooserId(eventId, participantId)
        choiceRepository.saveAll(choices)
    }

    @Transactional
    fun processMatching(eventId: Long) {
        val choices = choiceRepository.findByEventId(eventId)
        val choiceMap: Map<Long, Set<Long>> = choices
            .groupBy { it.chooser.id }
            .mapValues { (_, c) -> c.map { it.chosen.id }.toSet() }

        val createdPairs = mutableSetOf<Pair<Long, Long>>()

        choices.forEach { choice ->
            val chooserId = choice.chooser.id
            val chosenId = choice.chosen.id

            if (choiceMap[chosenId]?.contains(chooserId) == true) {
                val chooserProfile = profileRepository.findByParticipantId(chooserId).orElse(null)
                val chosenProfile = profileRepository.findByParticipantId(chosenId).orElse(null)

                if (chooserProfile != null && chosenProfile != null) {
                    val maleId = if (chooserProfile.gender == Gender.MALE) chooserId else chosenId
                    val femaleId = if (chooserProfile.gender == Gender.FEMALE) chooserId else chosenId
                    val pair = Pair(maleId, femaleId)

                    if (!createdPairs.contains(pair) &&
                        !matchResultRepository.existsByEventIdAndMember1IdAndMember2Id(eventId, maleId, femaleId)) {
                        val event = eventRepository.findById(eventId).orElseThrow { NotFoundException("이벤트를 찾을 수 없습니다") }
                        val male = participantRepository.findById(maleId).orElseThrow { NotFoundException("참가자를 찾을 수 없습니다") }
                        val female = participantRepository.findById(femaleId).orElseThrow { NotFoundException("참가자를 찾을 수 없습니다") }
                        matchResultRepository.save(MatchResult(event = event, member1 = male, member2 = female))
                        createdPairs.add(pair)
                    }
                }
            }
        }
    }

    fun getMatchResult(eventId: Long, participantId: Long): List<MatchResultResponse> {
        return matchResultRepository.findByEventIdAndParticipantId(eventId, participantId).map { result ->
            val matched = if (result.member1.id == participantId) result.member2 else result.member1
            MatchResultResponse(
                matchResultId = result.id, eventId = result.event.id,
                matchedParticipantId = matched.id, matchedNickname = matched.nickname,
                notified = result.notified
            )
        }
    }
}
