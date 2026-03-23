package com.blinddate.cafe.repository

import com.blinddate.cafe.entity.Cafe
import org.springframework.data.jpa.repository.JpaRepository

interface CafeRepository : JpaRepository<Cafe, Long> {
    fun findBySlug(slug: String): Cafe?
    fun findByIsActiveTrue(): List<Cafe>
    fun existsBySlug(slug: String): Boolean
}
