package com.blinddate.admin.dto

data class CreateCafeRequest(
    val name: String,
    val address: String,
    val slug: String,
    val description: String = "",
    val commissionRate: Int = 10
)

data class CreateCafeOwnerRequest(
    val cafeId: Long,
    val name: String,
    val phoneNumber: String,
    val email: String,
    val password: String,
    val kakaoId: String = ""
)

data class CreateOrganizerRequest(
    val name: String,
    val phoneNumber: String,
    val email: String,
    val password: String,
    val description: String = "",
    val commissionRate: Int = 15,
    val kakaoId: String = ""
)

data class OrganizerResponse(
    val id: Long,
    val name: String,
    val phoneNumber: String,
    val email: String,
    val description: String,
    val commissionRate: Int
)

data class DashboardResponse(
    val totalCafes: Long,
    val activeCafes: Long,
    val totalOrganizers: Long,
    val totalEvents: Long,
    val totalParticipants: Long,
    val totalCommissionAmount: Long,
    val pendingCommissionAmount: Long
)
