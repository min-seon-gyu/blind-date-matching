package com.blinddate.matching.dto

data class ParticipantNumberResponse(
    val memberId: Long,
    val number: Int,
    val gender: String
)

data class SubmitChoicesRequest(
    val chosenMemberIds: List<Long>
)

data class MatchResultResponse(
    val matchResultId: Long,
    val eventId: Long,
    val matchedMemberId: Long,
    val matchedMemberNickname: String,
    val notified: Boolean
)
