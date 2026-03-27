package com.blinddate.partnership.repository

import com.blinddate.partnership.entity.Partnership
import com.blinddate.partnership.entity.PartnershipStatus
import org.springframework.data.jpa.repository.JpaRepository

interface PartnershipRepository : JpaRepository<Partnership, Long> {
    fun findByCafeIdAndStatus(cafeId: Long, status: PartnershipStatus): List<Partnership>
    fun findByOrganizerIdAndStatus(organizerId: Long, status: PartnershipStatus): List<Partnership>
    fun findByCafeId(cafeId: Long): List<Partnership>
    fun findByOrganizerId(organizerId: Long): List<Partnership>
    fun findByCafeIdAndOrganizerIdAndStatusIn(cafeId: Long, organizerId: Long, statuses: List<PartnershipStatus>): Partnership?
}
