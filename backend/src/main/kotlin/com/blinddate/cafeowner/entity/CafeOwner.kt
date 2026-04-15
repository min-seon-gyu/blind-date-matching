package com.blinddate.cafeowner.entity

import com.blinddate.cafe.entity.Cafe
import com.blinddate.common.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "cafe_owner")
class CafeOwner(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id", nullable = false)
    val cafe: Cafe,

    @Column(nullable = false) var name: String,
    @Column(nullable = false) var phoneNumber: String,
    @Column(unique = true, nullable = false) val email: String,
    @Column var kakaoId: String = "",
    @Column(nullable = false) var password: String
) : BaseEntity()
