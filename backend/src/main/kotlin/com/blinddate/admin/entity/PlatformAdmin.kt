package com.blinddate.admin.entity

import com.blinddate.common.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "platform_admin")
class PlatformAdmin(
    @Column(unique = true, nullable = false) val email: String,
    @Column(nullable = false) var password: String,
    @Column(nullable = false) var name: String
) : BaseEntity()
