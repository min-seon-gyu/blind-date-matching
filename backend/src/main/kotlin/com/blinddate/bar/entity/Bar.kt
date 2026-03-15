package com.blinddate.bar.entity

import com.blinddate.common.entity.BaseEntity
import jakarta.persistence.*
import java.time.LocalTime

@Entity
@Table(name = "bar")
class Bar(
    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val address: String,

    @Column(nullable = false)
    val totalSeats: Int,

    @Column(nullable = false)
    var currentMaleCount: Int = 0,

    @Column(nullable = false)
    var currentFemaleCount: Int = 0,

    @Column(nullable = false)
    var isOpen: Boolean = false,

    @Column(nullable = false)
    val openTime: LocalTime,

    @Column(nullable = false)
    val closeTime: LocalTime
) : BaseEntity()
