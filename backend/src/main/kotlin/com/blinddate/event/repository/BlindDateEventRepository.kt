package com.blinddate.event.repository

import com.blinddate.event.entity.BlindDateEvent
import com.blinddate.event.entity.EventStatus
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface BlindDateEventRepository : JpaRepository<BlindDateEvent, Long> {
    @Query("SELECT e FROM BlindDateEvent e WHERE e.deletedAt IS NULL AND YEAR(e.date) = :year AND MONTH(e.date) = :month ORDER BY e.date, e.time")
    fun findByYearAndMonth(year: Int, month: Int): List<BlindDateEvent>

    @Query("SELECT e FROM BlindDateEvent e WHERE e.deletedAt IS NULL AND e.status = :status")
    fun findByStatus(status: EventStatus): List<BlindDateEvent>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM BlindDateEvent e WHERE e.id = :id")
    fun findByIdForUpdate(id: Long): Optional<BlindDateEvent>
}
