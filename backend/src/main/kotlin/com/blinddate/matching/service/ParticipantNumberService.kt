package com.blinddate.matching.service

import com.blinddate.application.entity.ApplicationStatus
import com.blinddate.application.repository.ApplicationRepository
import com.blinddate.matching.entity.ParticipantNumber
import com.blinddate.matching.repository.ParticipantNumberRepository
import com.blinddate.participant.repository.ParticipantProfileRepository
import com.blinddate.event.repository.EventRepository
import com.blinddate.common.exception.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ParticipantNumberService(
    private val participantNumberRepository: ParticipantNumberRepository,
    private val applicationRepository: ApplicationRepository,
    private val profileRepository: ParticipantProfileRepository,
    private val eventRepository: EventRepository
) {
    @Transactional
    fun assignNumbers(eventId: Long) {
        if (participantNumberRepository.existsByEventId(eventId)) return

        val event = eventRepository.findById(eventId).orElseThrow { NotFoundException("이벤트를 찾을 수 없습니다") }
        val approvedApps = applicationRepository.findByEventIdAndStatus(eventId, ApplicationStatus.APPROVED)

        var maleNum = 1
        var femaleNum = 1

        approvedApps.forEach { app ->
            val profile = profileRepository.findByParticipantId(app.participant.id).orElse(null) ?: return@forEach
            val number = when (profile.gender) {
                com.blinddate.participant.entity.Gender.MALE -> maleNum++
                com.blinddate.participant.entity.Gender.FEMALE -> femaleNum++
            }
            participantNumberRepository.save(
                ParticipantNumber(event = event, participant = app.participant, number = number, gender = profile.gender)
            )
        }
    }
}
