package com.blinddate.bar.repository
import com.blinddate.bar.entity.Bar
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
interface BarRepository : JpaRepository<Bar, Long> {
    fun findBySlug(slug: String): Optional<Bar>
    fun existsBySlug(slug: String): Boolean
}
