package com.blinddate.commission.repository

import com.blinddate.commission.entity.Commission
import com.blinddate.commission.entity.CommissionStatus
import com.blinddate.commission.entity.CommissionTargetType
import org.springframework.data.jpa.repository.JpaRepository

interface CommissionRepository : JpaRepository<Commission, Long> {
    fun findByCafeId(cafeId: Long): List<Commission>
    fun findByTargetTypeAndTargetId(targetType: CommissionTargetType, targetId: Long): List<Commission>
    fun findByEventId(eventId: Long): List<Commission>
    fun findByStatus(status: CommissionStatus): List<Commission>
    fun existsByEventId(eventId: Long): Boolean
}
