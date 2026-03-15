package com.blinddate.member.repository

import com.blinddate.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface MemberRepository : JpaRepository<Member, Long> {
    fun findByKakaoId(kakaoId: String): Optional<Member>
}
