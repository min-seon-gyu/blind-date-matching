package com.blinddate.barowner.repository
import com.blinddate.barowner.entity.BarOwner
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
interface BarOwnerRepository : JpaRepository<BarOwner, Long> {
    fun findByEmail(email: String): Optional<BarOwner>
    fun findByBarId(barId: Long): Optional<BarOwner>
}
