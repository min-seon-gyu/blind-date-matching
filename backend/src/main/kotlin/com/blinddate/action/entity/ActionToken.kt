package com.blinddate.action.entity

import com.blinddate.common.entity.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "action_token")
class ActionToken(
    @Column(unique = true, nullable = false)
    val token: String = UUID.randomUUID().toString(),

    @Column(nullable = false) val actionType: String,
    @Column(nullable = false) val targetId: Long,
    @Column(nullable = false) val barOwnerId: Long,
    @Column(nullable = false) var used: Boolean = false,
    @Column(nullable = false) val expiresAt: LocalDateTime = LocalDateTime.now().plusHours(24)
) : BaseEntity() {
    fun isExpired() = LocalDateTime.now().isAfter(expiresAt)
    fun isValid() = !used && !isExpired()
}
