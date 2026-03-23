package com.blinddate.matching.dto

data class ParticipantInfoResponse(
    val participantId: Long,
    val number: Int,
    val gender: String,
    val age: Int,
    val job: String,
    val introduction: String
)

data class ChoiceRequest(val chosenParticipantIds: List<Long>)

data class MatchResultResponse(
    val matchResultId: Long,
    val eventId: Long,
    val matchedParticipantId: Long,
    val matchedNickname: String,
    val notified: Boolean
)
