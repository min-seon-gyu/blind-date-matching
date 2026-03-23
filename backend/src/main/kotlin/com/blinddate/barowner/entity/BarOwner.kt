package com.blinddate.barowner.entity

import com.blinddate.bar.entity.Bar
import com.blinddate.common.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "bar_owner")
class BarOwner(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bar_id", nullable = false)
    val bar: Bar,

    @Column(nullable = false) var name: String,
    @Column(nullable = false) var phoneNumber: String,
    @Column(unique = true, nullable = false) val email: String,
    @Column var kakaoId: String = "",
    @Column(nullable = false) var password: String
) : BaseEntity()
