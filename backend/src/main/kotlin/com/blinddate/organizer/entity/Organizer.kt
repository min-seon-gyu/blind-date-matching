package com.blinddate.organizer.entity

import com.blinddate.common.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "organizer")
class Organizer(
    @Column(nullable = false) var name: String,
    @Column(nullable = false) var phoneNumber: String,
    @Column(unique = true, nullable = false) val email: String,
    @Column var kakaoId: String = "",
    @Column(nullable = false) var password: String,
    @Column(columnDefinition = "TEXT") var description: String = "",
    @Column(nullable = false) var commissionRate: Int = 15
) : BaseEntity()
