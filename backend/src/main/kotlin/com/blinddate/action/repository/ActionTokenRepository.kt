package com.blinddate.action.repository

import com.blinddate.action.entity.ActionToken
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface ActionTokenRepository : JpaRepository<ActionToken, Long> {
    fun findByToken(token: String): Optional<ActionToken>
}
