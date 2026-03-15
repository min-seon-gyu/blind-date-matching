package com.blinddate.member.repository

import com.blinddate.member.entity.MemberProfile
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface MemberProfileRepository : JpaRepository<MemberProfile, Long> {
    fun findByMemberId(memberId: Long): Optional<MemberProfile>
    fun existsByMemberId(memberId: Long): Boolean
}
