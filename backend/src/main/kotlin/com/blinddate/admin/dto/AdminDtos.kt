package com.blinddate.admin.dto

data class CreateBarRequest(
    val name: String,
    val address: String,
    val slug: String,
    val description: String = "",
    val commissionRate: Int = 10
)

data class CreateBarOwnerRequest(
    val barId: Long,
    val name: String,
    val phoneNumber: String,
    val email: String,
    val password: String,
    val kakaoId: String = ""
)

data class DashboardResponse(
    val totalBars: Long,
    val activeBars: Long,
    val totalEvents: Long,
    val totalParticipants: Long,
    val totalCommissionAmount: Long,
    val pendingCommissionAmount: Long
)
