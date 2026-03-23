package com.blinddate.participant.dto

import com.blinddate.participant.entity.*

data class ParticipantResponse(
    val id: Long, val nickname: String, val phoneNumber: String, val isProfileComplete: Boolean
)

data class ProfileCreateRequest(
    val name: String, val age: Int, val gender: Gender, val job: String,
    val height: Int = 0, val mbti: String = "", val hobby: String = "",
    val drinking: DrinkingType = DrinkingType.NONE, val smoking: SmokingType = SmokingType.NONE,
    val religion: String = "", val idealType: String = "", val introduction: String = ""
)

data class ProfileResponse(
    val id: Long, val name: String, val age: Int, val gender: Gender, val job: String,
    val height: Int, val mbti: String, val hobby: String,
    val drinking: DrinkingType, val smoking: SmokingType, val religion: String,
    val idealType: String, val introduction: String, val photoUrl: String
)
