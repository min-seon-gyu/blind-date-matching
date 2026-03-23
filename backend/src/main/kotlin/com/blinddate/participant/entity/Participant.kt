package com.blinddate.participant.entity

import com.blinddate.common.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "participant")
class Participant(
    @Column(unique = true, nullable = false)
    val kakaoId: String,

    @Column
    var nickname: String = "",

    @Column
    var phoneNumber: String = "",

    @Column(nullable = false)
    var isProfileComplete: Boolean = false
) : BaseEntity()
