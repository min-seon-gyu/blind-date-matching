package com.blinddate.bar.entity

import com.blinddate.common.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "bar")
class Bar(
    @Column(nullable = false) var name: String,
    @Column(nullable = false) var address: String,
    @Column(columnDefinition = "TEXT") var description: String = "",
    @Column var logoUrl: String = "",
    @Column var coverImageUrl: String = "",
    @Column(unique = true, nullable = false) val slug: String,
    @Column(nullable = false) var commissionRate: Int = 10,
    @Column(nullable = false) var isActive: Boolean = true
) : BaseEntity()
