package com.blinddate.admin.repository
import com.blinddate.admin.entity.PlatformAdmin
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
interface PlatformAdminRepository : JpaRepository<PlatformAdmin, Long> {
    fun findByEmail(email: String): Optional<PlatformAdmin>
}
