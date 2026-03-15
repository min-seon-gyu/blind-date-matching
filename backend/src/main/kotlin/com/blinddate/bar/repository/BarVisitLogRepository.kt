package com.blinddate.bar.repository

import com.blinddate.bar.entity.BarVisitLog
import org.springframework.data.jpa.repository.JpaRepository

interface BarVisitLogRepository : JpaRepository<BarVisitLog, Long> {
    fun findByBarIdAndCheckOutAtIsNull(barId: Long): List<BarVisitLog>
}
