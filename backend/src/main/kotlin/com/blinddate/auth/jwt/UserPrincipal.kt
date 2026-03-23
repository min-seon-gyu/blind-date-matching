package com.blinddate.auth.jwt
data class UserPrincipal(
    val id: Long,
    val userType: UserType,
    val barId: Long? = null
)
