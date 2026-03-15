package com.blinddate.member.dto

import com.blinddate.member.entity.*
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class MemberResponse(
    val id: Long, val email: String, val nickname: String,
    val phoneNumber: String, val role: Role, val hasProfile: Boolean
)

data class ProfileCreateRequest(
    @field:NotBlank val name: String,
    @field:Min(18) val age: Int,
    val gender: Gender,
    @field:NotBlank val job: String,
    val height: Int = 0, val mbti: String = "", val hobby: String = "",
    val drinking: DrinkingType = DrinkingType.NONE,
    val smoking: SmokingType = SmokingType.NONE,
    val religion: String = "", val idealType: String = "", val introduction: String = ""
)

data class ProfileResponse(
    val id: Long, val name: String, val age: Int, val gender: Gender,
    val job: String, val height: Int, val mbti: String, val hobby: String,
    val drinking: DrinkingType, val smoking: SmokingType, val religion: String,
    val idealType: String, val introduction: String, val photoUrl: String
)
