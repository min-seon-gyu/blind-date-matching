package com.blinddate.organizer.repository

import com.blinddate.organizer.entity.Organizer
import org.springframework.data.jpa.repository.JpaRepository

interface OrganizerRepository : JpaRepository<Organizer, Long> {
    fun findByEmail(email: String): Organizer?
}
