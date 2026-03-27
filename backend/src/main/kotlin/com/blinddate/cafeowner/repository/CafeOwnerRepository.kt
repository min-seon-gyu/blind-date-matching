package com.blinddate.cafeowner.repository

import com.blinddate.cafeowner.entity.CafeOwner
import org.springframework.data.jpa.repository.JpaRepository

interface CafeOwnerRepository : JpaRepository<CafeOwner, Long> {
    fun findByEmail(email: String): CafeOwner?
    fun findByCafeId(cafeId: Long): List<CafeOwner>
}
