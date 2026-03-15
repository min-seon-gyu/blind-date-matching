package com.blinddate.member.entity

import com.blinddate.common.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "member")
class Member(
    @Column(unique = true, nullable = false)
    val kakaoId: String,

    @Column
    var email: String = "",

    @Column
    var nickname: String = "",

    @Column
    var phoneNumber: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role = Role.USER
) : BaseEntity()
