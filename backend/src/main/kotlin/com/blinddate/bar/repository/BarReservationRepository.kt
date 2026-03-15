package com.blinddate.bar.repository

import com.blinddate.bar.entity.BarReservation
import com.blinddate.bar.entity.BarReservationStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface BarReservationRepository : JpaRepository<BarReservation, Long> {
    fun findByMemberIdOrderByDateDesc(memberId: Long): List<BarReservation>
    fun findByBarIdAndDateAndStatus(barId: Long, date: LocalDate, status: BarReservationStatus): List<BarReservation>
}
