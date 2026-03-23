# B2B Blind Date Platform - Backend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** B2B 멀티테넌트 소개팅 매칭 플랫폼의 백엔드 API를 처음부터 구축한다.

**Architecture:** 기존 단일 테넌트 코드를 전부 교체하고, Bar 중심 멀티테넌트 구조로 재설계한다. 3개 사용자 유형(Participant, BarOwner, PlatformAdmin)을 단일 JWT 체계로 인증하며, 도메인별 패키지로 분리한다.

**Tech Stack:** Kotlin 1.9, Spring Boot 3.2, Spring Data JPA, Spring Security, MySQL 8.0, Redis 7, jjwt 0.12, MockK

**Spec:** `docs/superpowers/specs/2026-03-23-b2b-blind-date-platform-design.md`

---

## File Structure

```
backend/src/main/kotlin/com/blinddate/
├── BlindDateApplication.kt
├── common/
│   ├── config/
│   │   ├── JpaConfig.kt              ← JPA auditing 설정
│   │   ├── RedisConfig.kt            ← Redis 설정
│   │   └── SecurityConfig.kt         ← Spring Security 설정 (3개 사용자 유형)
│   ├── entity/
│   │   └── BaseEntity.kt             ← id, createdAt, updatedAt
│   ├── exception/
│   │   ├── Exceptions.kt             ← NotFoundException, BadRequestException 등
│   │   └── GlobalExceptionHandler.kt ← @RestControllerAdvice
│   └── dto/
│       └── CursorPageResponse.kt     ← 커서 기반 페이지네이션 응답
│
├── auth/
│   ├── jwt/
│   │   ├── JwtTokenProvider.kt       ← JWT 생성/검증 (userType claim 포함)
│   │   ├── JwtAuthenticationFilter.kt← OncePerRequestFilter
│   │   └── UserPrincipal.kt          ← SecurityContext principal (id, userType, barId?)
│   ├── controller/
│   │   └── AuthController.kt         ← POST /api/auth/kakao, /api/auth/refresh
│   ├── dto/
│   │   └── AuthDtos.kt
│   └── service/
│       ├── ParticipantAuthService.kt ← 카카오 로그인 + 토큰 발급
│       ├── BarOwnerAuthService.kt    ← 바 사장님 이메일/비밀번호 로그인
│       ├── AdminAuthService.kt       ← 관리자 이메일/비밀번호 로그인
│       └── KakaoOAuthService.kt      ← 카카오 API 호출
│
├── participant/
│   ├── entity/
│   │   ├── Participant.kt
│   │   ├── ParticipantProfile.kt
│   │   ├── Gender.kt
│   │   ├── DrinkingType.kt
│   │   └── SmokingType.kt
│   ├── repository/
│   │   ├── ParticipantRepository.kt
│   │   └── ParticipantProfileRepository.kt
│   ├── controller/
│   │   └── ParticipantController.kt  ← GET /api/me, POST/PUT /api/me/profile
│   ├── dto/
│   │   └── ParticipantDtos.kt
│   └── service/
│       └── ParticipantService.kt
│
├── bar/
│   ├── entity/
│   │   └── Bar.kt
│   ├── repository/
│   │   └── BarRepository.kt
│   ├── controller/
│   │   └── BarController.kt          ← GET /api/bars/{slug} (공개)
│   ├── dto/
│   │   └── BarDtos.kt
│   └── service/
│       └── BarService.kt
│
├── barowner/
│   ├── entity/
│   │   └── BarOwner.kt
│   ├── repository/
│   │   └── BarOwnerRepository.kt
│   ├── controller/
│   │   ├── BarOwnerAuthController.kt  ← POST /api/bar-owner/auth/login
│   │   ├── BarOwnerBarController.kt   ← GET/PUT /api/bar-owner/my-bar
│   │   ├── BarOwnerEventController.kt ← CRUD /api/bar-owner/events
│   │   └── BarOwnerApplicationController.kt ← approve/reject
│   ├── dto/
│   │   └── BarOwnerDtos.kt
│   └── service/
│       └── BarOwnerService.kt
│
├── event/
│   ├── entity/
│   │   ├── Event.kt
│   │   ├── EventStatus.kt
│   │   └── MatchingMode.kt
│   ├── repository/
│   │   └── EventRepository.kt
│   ├── controller/
│   │   └── EventController.kt        ← GET /api/bars/{slug}/events (공개)
│   ├── dto/
│   │   └── EventDtos.kt
│   └── service/
│       └── EventService.kt
│
├── application/
│   ├── entity/
│   │   ├── Application.kt
│   │   └── ApplicationStatus.kt
│   ├── repository/
│   │   └── ApplicationRepository.kt
│   ├── controller/
│   │   └── ApplicationController.kt  ← POST/DELETE /api/events/{id}/apply
│   ├── dto/
│   │   └── ApplicationDtos.kt
│   └── service/
│       └── ApplicationService.kt
│
├── matching/
│   ├── entity/
│   │   ├── ParticipantNumber.kt
│   │   ├── Choice.kt
│   │   └── MatchResult.kt
│   ├── repository/
│   │   ├── ParticipantNumberRepository.kt
│   │   ├── ChoiceRepository.kt
│   │   └── MatchResultRepository.kt
│   ├── controller/
│   │   └── MatchingController.kt     ← participants, choices, result
│   ├── dto/
│   │   └── MatchingDtos.kt
│   └── service/
│       ├── ParticipantNumberService.kt
│       └── MatchingService.kt
│
├── notification/
│   ├── entity/
│   │   ├── Notification.kt
│   │   ├── NotificationType.kt
│   │   └── RecipientType.kt
│   ├── repository/
│   │   └── NotificationRepository.kt
│   ├── controller/
│   │   └── NotificationController.kt ← GET /api/me/notifications
│   ├── dto/
│   │   └── NotificationDtos.kt
│   └── service/
│       ├── NotificationService.kt
│       └── KakaoChannelMessageService.kt
│
├── action/
│   ├── entity/
│   │   └── ActionToken.kt
│   ├── repository/
│   │   └── ActionTokenRepository.kt
│   ├── controller/
│   │   └── ActionController.kt       ← GET/POST /api/actions/{token}
│   ├── dto/
│   │   └── ActionDtos.kt
│   └── service/
│       └── ActionTokenService.kt
│
├── commission/
│   ├── entity/
│   │   ├── Commission.kt
│   │   └── CommissionStatus.kt
│   ├── repository/
│   │   └── CommissionRepository.kt
│   ├── dto/
│   │   └── CommissionDtos.kt
│   └── service/
│       └── CommissionService.kt
│
├── admin/
│   ├── entity/
│   │   └── PlatformAdmin.kt
│   ├── repository/
│   │   └── PlatformAdminRepository.kt
│   ├── controller/
│   │   ├── AdminAuthController.kt
│   │   ├── AdminBarController.kt
│   │   ├── AdminCommissionController.kt
│   │   └── AdminDashboardController.kt
│   ├── dto/
│   │   └── AdminDtos.kt
│   └── service/
│       └── AdminService.kt
│
└── scheduler/
    ├── EventStatusScheduler.kt
    ├── MatchingScheduler.kt
    └── ReminderScheduler.kt

backend/src/test/kotlin/com/blinddate/
├── auth/
│   ├── jwt/JwtTokenProviderTest.kt
│   └── service/AuthServiceTest.kt
├── participant/service/ParticipantServiceTest.kt
├── bar/service/BarServiceTest.kt
├── barowner/service/BarOwnerServiceTest.kt
├── event/service/EventServiceTest.kt
├── application/service/ApplicationServiceTest.kt
├── matching/service/
│   ├── ParticipantNumberServiceTest.kt
│   └── MatchingServiceTest.kt
├── notification/service/NotificationServiceTest.kt
├── action/service/ActionTokenServiceTest.kt
├── commission/service/CommissionServiceTest.kt
├── admin/service/AdminServiceTest.kt
└── scheduler/
    ├── EventStatusSchedulerTest.kt
    └── MatchingSchedulerTest.kt
```

---

## Task 1: 프로젝트 정리 & 공통 인프라

기존 소스코드를 제거하고 공통 모듈(BaseEntity, 예외, 설정)을 새로 구성한다.

**Files:**
- Delete: `backend/src/main/kotlin/com/blinddate/` (기존 전체)
- Delete: `backend/src/test/kotlin/com/blinddate/` (기존 전체)
- Create: `backend/src/main/kotlin/com/blinddate/BlindDateApplication.kt`
- Create: `backend/src/main/kotlin/com/blinddate/common/entity/BaseEntity.kt`
- Create: `backend/src/main/kotlin/com/blinddate/common/exception/Exceptions.kt`
- Create: `backend/src/main/kotlin/com/blinddate/common/exception/GlobalExceptionHandler.kt`
- Create: `backend/src/main/kotlin/com/blinddate/common/config/JpaConfig.kt`
- Create: `backend/src/main/kotlin/com/blinddate/common/config/RedisConfig.kt`
- Create: `backend/src/main/kotlin/com/blinddate/common/dto/CursorPageResponse.kt`
- Modify: `backend/src/main/resources/application.yml`
- Modify: `backend/src/main/resources/application-local.yml`
- Modify: `backend/src/main/resources/application-test.yml`

- [ ] **Step 1: 기존 소스코드 삭제**

```bash
rm -rf backend/src/main/kotlin/com/blinddate/*
rm -rf backend/src/test/kotlin/com/blinddate/*
```

- [ ] **Step 2: Application 진입점 생성**

```kotlin
// BlindDateApplication.kt
package com.blinddate

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class BlindDateApplication

fun main(args: Array<String>) {
    runApplication<BlindDateApplication>(*args)
}
```

- [ ] **Step 3: BaseEntity 생성**

```kotlin
// common/entity/BaseEntity.kt
package com.blinddate.common.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
}
```

- [ ] **Step 4: 예외 클래스 + 글로벌 핸들러 생성**

```kotlin
// common/exception/Exceptions.kt
package com.blinddate.common.exception

open class BusinessException(val status: Int, override val message: String) : RuntimeException(message)
class NotFoundException(message: String) : BusinessException(404, message)
class BadRequestException(message: String) : BusinessException(400, message)
class ConflictException(message: String) : BusinessException(409, message)
class ForbiddenException(message: String) : BusinessException(403, message)
class UnauthorizedException(message: String) : BusinessException(401, message)
```

```kotlin
// common/exception/GlobalExceptionHandler.kt
package com.blinddate.common.exception

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    data class ErrorResponse(val status: Int, val message: String)

    @ExceptionHandler(BusinessException::class)
    fun handleBusiness(e: BusinessException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(e.status).body(ErrorResponse(e.status, e.message))
}
```

- [ ] **Step 5: JpaConfig, RedisConfig, CursorPageResponse 생성**

```kotlin
// common/config/JpaConfig.kt
package com.blinddate.common.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@Configuration
@EnableJpaAuditing
class JpaConfig
```

```kotlin
// common/config/RedisConfig.kt
package com.blinddate.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {
    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, String> =
        RedisTemplate<String, String>().apply {
            setConnectionFactory(connectionFactory)
            keySerializer = StringRedisSerializer()
            valueSerializer = StringRedisSerializer()
        }
}
```

```kotlin
// common/dto/CursorPageResponse.kt
package com.blinddate.common.dto

data class CursorPageResponse<T>(
    val content: List<T>,
    val nextCursor: Long?,
    val hasNext: Boolean
)
```

- [ ] **Step 6: application 설정 파일 업데이트**

`application.yml`은 기존 유지. `application-local.yml`에서 `jpa.hibernate.ddl-auto: update`로 초기 개발.

- [ ] **Step 7: 빌드 확인**

Run: `cd backend && ./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: 커밋**

```bash
git add -A && git commit -m "feat: clean slate with common infrastructure (BaseEntity, exceptions, configs)"
```

---

## Task 2: 핵심 엔티티 (Bar, BarOwner, Participant, PlatformAdmin)

모든 도메인의 기반이 되는 엔티티와 repository를 생성한다.

**Files:**
- Create: `backend/src/main/kotlin/com/blinddate/participant/entity/Gender.kt`
- Create: `backend/src/main/kotlin/com/blinddate/participant/entity/DrinkingType.kt`
- Create: `backend/src/main/kotlin/com/blinddate/participant/entity/SmokingType.kt`
- Create: `backend/src/main/kotlin/com/blinddate/participant/entity/Participant.kt`
- Create: `backend/src/main/kotlin/com/blinddate/participant/entity/ParticipantProfile.kt`
- Create: `backend/src/main/kotlin/com/blinddate/participant/repository/ParticipantRepository.kt`
- Create: `backend/src/main/kotlin/com/blinddate/participant/repository/ParticipantProfileRepository.kt`
- Create: `backend/src/main/kotlin/com/blinddate/bar/entity/Bar.kt`
- Create: `backend/src/main/kotlin/com/blinddate/bar/repository/BarRepository.kt`
- Create: `backend/src/main/kotlin/com/blinddate/barowner/entity/BarOwner.kt`
- Create: `backend/src/main/kotlin/com/blinddate/barowner/repository/BarOwnerRepository.kt`
- Create: `backend/src/main/kotlin/com/blinddate/admin/entity/PlatformAdmin.kt`
- Create: `backend/src/main/kotlin/com/blinddate/admin/repository/PlatformAdminRepository.kt`

- [ ] **Step 1: Enum 타입 생성**

```kotlin
// participant/entity/Gender.kt
package com.blinddate.participant.entity
enum class Gender { MALE, FEMALE }

// participant/entity/DrinkingType.kt
package com.blinddate.participant.entity
enum class DrinkingType { NONE, SOMETIMES, OFTEN }

// participant/entity/SmokingType.kt
package com.blinddate.participant.entity
enum class SmokingType { NONE, SOMETIMES, OFTEN }
```

- [ ] **Step 2: Participant + ParticipantProfile 엔티티**

```kotlin
// participant/entity/Participant.kt
package com.blinddate.participant.entity

import com.blinddate.common.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "participant")
class Participant(
    @Column(unique = true, nullable = false)
    val kakaoId: String,

    @Column
    var nickname: String = "",

    @Column
    var phoneNumber: String = "",

    @Column(nullable = false)
    var isProfileComplete: Boolean = false
) : BaseEntity()
```

```kotlin
// participant/entity/ParticipantProfile.kt
package com.blinddate.participant.entity

import com.blinddate.common.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "participant_profile")
class ParticipantProfile(
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", unique = true, nullable = false)
    val participant: Participant,

    @Column(nullable = false) var name: String,
    @Column(nullable = false) var age: Int,
    @Enumerated(EnumType.STRING) @Column(nullable = false) var gender: Gender,
    @Column(nullable = false) var job: String,
    @Column var height: Int = 0,
    @Column(length = 4) var mbti: String = "",
    @Column(columnDefinition = "TEXT") var hobby: String = "",
    @Enumerated(EnumType.STRING) var drinking: DrinkingType = DrinkingType.NONE,
    @Enumerated(EnumType.STRING) var smoking: SmokingType = SmokingType.NONE,
    @Column var religion: String = "",
    @Column(columnDefinition = "TEXT") var idealType: String = "",
    @Column(columnDefinition = "TEXT") var introduction: String = "",
    @Column var photoUrl: String = ""
) : BaseEntity()
```

- [ ] **Step 3: Bar 엔티티**

```kotlin
// bar/entity/Bar.kt
package com.blinddate.bar.entity

import com.blinddate.common.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "bar")
class Bar(
    @Column(nullable = false) var name: String,
    @Column(nullable = false) var address: String,
    @Column(columnDefinition = "TEXT") var description: String = "",
    @Column var logoUrl: String = "",
    @Column var coverImageUrl: String = "",
    @Column(unique = true, nullable = false) val slug: String,
    @Column(nullable = false) var commissionRate: Int = 10,
    @Column(nullable = false) var isActive: Boolean = true
) : BaseEntity()
```

- [ ] **Step 4: BarOwner 엔티티**

```kotlin
// barowner/entity/BarOwner.kt
package com.blinddate.barowner.entity

import com.blinddate.bar.entity.Bar
import com.blinddate.common.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "bar_owner")
class BarOwner(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bar_id", nullable = false)
    val bar: Bar,
    // Phase 1에서는 1:1이지만, Phase 2 확장을 위해 @ManyToOne 유지. 비즈니스 로직에서 1:1 강제.

    @Column(nullable = false) var name: String,
    @Column(nullable = false) var phoneNumber: String,
    @Column(unique = true, nullable = false) val email: String,
    @Column var kakaoId: String = "",
    @Column(nullable = false) var password: String
) : BaseEntity()
```

- [ ] **Step 5: PlatformAdmin 엔티티**

```kotlin
// admin/entity/PlatformAdmin.kt
package com.blinddate.admin.entity

import com.blinddate.common.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "platform_admin")
class PlatformAdmin(
    @Column(unique = true, nullable = false) val email: String,
    @Column(nullable = false) var password: String,
    @Column(nullable = false) var name: String
) : BaseEntity()
```

- [ ] **Step 6: Repository 인터페이스 생성**

```kotlin
// participant/repository/ParticipantRepository.kt
package com.blinddate.participant.repository
import com.blinddate.participant.entity.Participant
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
interface ParticipantRepository : JpaRepository<Participant, Long> {
    fun findByKakaoId(kakaoId: String): Optional<Participant>
    fun existsByKakaoId(kakaoId: String): Boolean
}

// participant/repository/ParticipantProfileRepository.kt
package com.blinddate.participant.repository
import com.blinddate.participant.entity.ParticipantProfile
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
interface ParticipantProfileRepository : JpaRepository<ParticipantProfile, Long> {
    fun findByParticipantId(participantId: Long): Optional<ParticipantProfile>
    fun existsByParticipantId(participantId: Long): Boolean
}

// bar/repository/BarRepository.kt
package com.blinddate.bar.repository
import com.blinddate.bar.entity.Bar
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
interface BarRepository : JpaRepository<Bar, Long> {
    fun findBySlug(slug: String): Optional<Bar>
    fun existsBySlug(slug: String): Boolean
}

// barowner/repository/BarOwnerRepository.kt
package com.blinddate.barowner.repository
import com.blinddate.barowner.entity.BarOwner
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
interface BarOwnerRepository : JpaRepository<BarOwner, Long> {
    fun findByEmail(email: String): Optional<BarOwner>
    fun findByBarId(barId: Long): Optional<BarOwner>
}

// admin/repository/PlatformAdminRepository.kt
package com.blinddate.admin.repository
import com.blinddate.admin.entity.PlatformAdmin
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
interface PlatformAdminRepository : JpaRepository<PlatformAdmin, Long> {
    fun findByEmail(email: String): Optional<PlatformAdmin>
}
```

- [ ] **Step 7: 빌드 확인**

Run: `cd backend && ./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: 커밋**

```bash
git add -A && git commit -m "feat: add core entities (Participant, Bar, BarOwner, PlatformAdmin)"
```

---

## Task 3: JWT 인증 인프라

3개 사용자 유형을 구분하는 JWT 토큰 생성/검증, 필터, SecurityContext principal을 구현한다.

**Files:**
- Create: `backend/src/main/kotlin/com/blinddate/auth/jwt/UserType.kt`
- Create: `backend/src/main/kotlin/com/blinddate/auth/jwt/UserPrincipal.kt`
- Create: `backend/src/main/kotlin/com/blinddate/auth/jwt/JwtTokenProvider.kt`
- Create: `backend/src/main/kotlin/com/blinddate/auth/jwt/JwtAuthenticationFilter.kt`
- Create: `backend/src/main/kotlin/com/blinddate/common/config/SecurityConfig.kt`
- Test: `backend/src/test/kotlin/com/blinddate/auth/jwt/JwtTokenProviderTest.kt`

- [ ] **Step 1: UserType enum + UserPrincipal 생성**

```kotlin
// auth/jwt/UserType.kt
package com.blinddate.auth.jwt
enum class UserType { PARTICIPANT, BAR_OWNER, PLATFORM_ADMIN }

// auth/jwt/UserPrincipal.kt
package com.blinddate.auth.jwt
data class UserPrincipal(
    val id: Long,
    val userType: UserType,
    val barId: Long? = null  // BAR_OWNER일 때만
)
```

- [ ] **Step 2: JwtTokenProvider 테스트 작성**

```kotlin
// test: auth/jwt/JwtTokenProviderTest.kt
package com.blinddate.auth.jwt

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class JwtTokenProviderTest {
    private lateinit var provider: JwtTokenProvider

    @BeforeEach
    fun setUp() {
        provider = JwtTokenProvider(
            secret = "test-secret-key-must-be-at-least-256-bits-long-for-hs256",
            accessTokenExpiry = 1800000,
            refreshTokenExpiry = 1209600000
        )
    }

    @Test
    fun `should create and validate participant token`() {
        val token = provider.createAccessToken(1L, UserType.PARTICIPANT)
        assertTrue(provider.validateToken(token))
        val principal = provider.getUserPrincipal(token)
        assertEquals(1L, principal.id)
        assertEquals(UserType.PARTICIPANT, principal.userType)
        assertNull(principal.barId)
    }

    @Test
    fun `should create bar owner token with barId`() {
        val token = provider.createAccessToken(2L, UserType.BAR_OWNER, barId = 10L)
        val principal = provider.getUserPrincipal(token)
        assertEquals(2L, principal.id)
        assertEquals(UserType.BAR_OWNER, principal.userType)
        assertEquals(10L, principal.barId)
    }

    @Test
    fun `should reject expired token`() {
        val provider = JwtTokenProvider(
            secret = "test-secret-key-must-be-at-least-256-bits-long-for-hs256",
            accessTokenExpiry = -1000,
            refreshTokenExpiry = -1000
        )
        val token = provider.createAccessToken(1L, UserType.PARTICIPANT)
        assertFalse(provider.validateToken(token))
    }
}
```

- [ ] **Step 3: 테스트 실패 확인**

Run: `cd backend && ./gradlew test --tests "com.blinddate.auth.jwt.JwtTokenProviderTest"`
Expected: FAIL (class not found)

- [ ] **Step 4: JwtTokenProvider 구현**

```kotlin
// auth/jwt/JwtTokenProvider.kt
package com.blinddate.auth.jwt

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.access-token-expiry}") private val accessTokenExpiry: Long,
    @Value("\${jwt.refresh-token-expiry}") private val refreshTokenExpiry: Long
) {
    private val key: SecretKey by lazy { Keys.hmacShaKeyFor(secret.toByteArray()) }

    fun createAccessToken(userId: Long, userType: UserType, barId: Long? = null): String =
        buildToken(userId, userType, barId, accessTokenExpiry, "access")

    fun createRefreshToken(userId: Long, userType: UserType): String =
        buildToken(userId, userType, null, refreshTokenExpiry, "refresh")

    fun validateToken(token: String): Boolean = try {
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token)
        true
    } catch (e: Exception) {
        false
    }

    fun getUserPrincipal(token: String): UserPrincipal {
        val claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
        return UserPrincipal(
            id = claims.subject.toLong(),
            userType = UserType.valueOf(claims["userType"] as String),
            barId = (claims["barId"] as? Number)?.toLong()
        )
    }

    fun validateRefreshToken(token: String): Boolean =
        validateToken(token) && getTokenType(token) == "refresh"

    private fun getTokenType(token: String): String {
        val claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
        return claims["tokenType"] as? String ?: "access"
    }

    private fun buildToken(userId: Long, userType: UserType, barId: Long?, expiry: Long, tokenType: String): String {
        val now = Date()
        val builder = Jwts.builder()
            .subject(userId.toString())
            .claim("userType", userType.name)
            .claim("tokenType", tokenType)
            .issuedAt(now)
            .expiration(Date(now.time + expiry))
            .signWith(key)

        barId?.let { builder.claim("barId", it) }
        return builder.compact()
    }
}
```

- [ ] **Step 5: 테스트 통과 확인**

Run: `cd backend && ./gradlew test --tests "com.blinddate.auth.jwt.JwtTokenProviderTest"`
Expected: PASS (3 tests)

- [ ] **Step 6: JwtAuthenticationFilter 구현**

```kotlin
// auth/jwt/JwtAuthenticationFilter.kt
package com.blinddate.auth.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = resolveToken(request)
        if (token != null && jwtTokenProvider.validateToken(token)) {
            val principal = jwtTokenProvider.getUserPrincipal(token)
            val authorities = listOf(SimpleGrantedAuthority("ROLE_${principal.userType.name}"))
            val auth = UsernamePasswordAuthenticationToken(principal, null, authorities)
            SecurityContextHolder.getContext().authentication = auth
        }
        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val bearer = request.getHeader("Authorization") ?: return null
        return if (bearer.startsWith("Bearer ")) bearer.substring(7) else null
    }
}
```

- [ ] **Step 7: SecurityConfig 구현**

```kotlin
// common/config/SecurityConfig.kt
package com.blinddate.common.config

import com.blinddate.auth.jwt.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(private val jwtAuthenticationFilter: JwtAuthenticationFilter) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/bar-owner/auth/**").permitAll()
                    .requestMatchers("/api/admin/auth/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/bars/**").permitAll()
                    .requestMatchers("/api/actions/**").permitAll()
                    .requestMatchers("/api/bar-owner/**").hasRole("BAR_OWNER")
                    .requestMatchers("/api/admin/**").hasRole("PLATFORM_ADMIN")
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            allowedOrigins = listOf("http://localhost:5173", "http://localhost:3000")
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE")
            allowedHeaders = listOf("*")
            allowCredentials = true
        }
        return UrlBasedCorsConfigurationSource().apply { registerCorsConfiguration("/**", config) }
    }
}
```

- [ ] **Step 8: 커밋**

```bash
git add -A && git commit -m "feat: add JWT auth infrastructure with multi-user type support"
```

---

## Task 4: 인증 엔드포인트 (카카오 OAuth + BarOwner/Admin 로그인)

카카오 OAuth를 통한 참가자 로그인과 이메일/비밀번호 기반 BarOwner/Admin 로그인을 구현한다.

**Files:**
- Create: `backend/src/main/kotlin/com/blinddate/auth/dto/AuthDtos.kt`
- Create: `backend/src/main/kotlin/com/blinddate/auth/service/KakaoOAuthService.kt`
- Create: `backend/src/main/kotlin/com/blinddate/auth/service/AuthService.kt`
- Create: `backend/src/main/kotlin/com/blinddate/auth/controller/AuthController.kt`
- Create: `backend/src/main/kotlin/com/blinddate/barowner/controller/BarOwnerAuthController.kt`
- Create: `backend/src/main/kotlin/com/blinddate/admin/controller/AdminAuthController.kt`
- Test: `backend/src/test/kotlin/com/blinddate/auth/service/AuthServiceTest.kt`

- [ ] **Step 1: AuthDtos 생성**

```kotlin
// auth/dto/AuthDtos.kt
package com.blinddate.auth.dto

data class KakaoLoginRequest(val code: String)
data class EmailLoginRequest(val email: String, val password: String)
data class TokenResponse(val accessToken: String, val refreshToken: String, val isNewUser: Boolean = false)
data class RefreshRequest(val refreshToken: String)
```

- [ ] **Step 2: BarOwnerAuthService 테스트 작성**

```kotlin
// test: auth/service/BarOwnerAuthServiceTest.kt
package com.blinddate.auth.service

import com.blinddate.auth.jwt.JwtTokenProvider
import com.blinddate.auth.jwt.UserType
import com.blinddate.barowner.entity.BarOwner
import com.blinddate.barowner.repository.BarOwnerRepository
import com.blinddate.bar.entity.Bar
import com.blinddate.common.exception.UnauthorizedException
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.Optional

class BarOwnerAuthServiceTest {
    private val barOwnerRepository = mockk<BarOwnerRepository>()
    private val jwtTokenProvider = JwtTokenProvider(
        secret = "test-secret-key-must-be-at-least-256-bits-long-for-hs256",
        accessTokenExpiry = 1800000,
        refreshTokenExpiry = 1209600000
    )
    private val passwordEncoder = BCryptPasswordEncoder()

    private val service = BarOwnerAuthService(barOwnerRepository, jwtTokenProvider, passwordEncoder)

    @Test
    fun `login should return tokens for valid credentials`() {
        val bar = Bar(name = "Test Bar", address = "addr", slug = "test-bar")
        val owner = BarOwner(
            bar = bar, name = "Owner", phoneNumber = "010",
            email = "owner@test.com", password = passwordEncoder.encode("pass123")
        )
        every { barOwnerRepository.findByEmail("owner@test.com") } returns Optional.of(owner)

        val result = service.login("owner@test.com", "pass123")
        assertNotNull(result.accessToken)
        assertNotNull(result.refreshToken)
    }

    @Test
    fun `login should throw for wrong password`() {
        val bar = Bar(name = "Test Bar", address = "addr", slug = "test-bar")
        val owner = BarOwner(
            bar = bar, name = "Owner", phoneNumber = "010",
            email = "owner@test.com", password = passwordEncoder.encode("pass123")
        )
        every { barOwnerRepository.findByEmail("owner@test.com") } returns Optional.of(owner)

        assertThrows(UnauthorizedException::class.java) {
            service.login("owner@test.com", "wrong")
        }
    }
}
```

- [ ] **Step 3: 테스트 실패 확인**

Run: `cd backend && ./gradlew test --tests "com.blinddate.auth.service.BarOwnerAuthServiceTest"`
Expected: FAIL

- [ ] **Step 4: KakaoOAuthService 구현**

```kotlin
// auth/service/KakaoOAuthService.kt
package com.blinddate.auth.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class KakaoOAuthService(
    @Value("\${kakao.client-id}") private val clientId: String,
    @Value("\${kakao.redirect-uri}") private val redirectUri: String
) {
    private val webClient = WebClient.create()

    data class KakaoTokenResponse(val access_token: String)
    data class KakaoUserResponse(val id: Long, val kakao_account: KakaoAccount?)
    data class KakaoAccount(val email: String?, val profile: KakaoProfile?)
    data class KakaoProfile(val nickname: String?)

    fun getAccessToken(code: String): String {
        val response = webClient.post()
            .uri("https://kauth.kakao.com/oauth/token")
            .bodyValue("grant_type=authorization_code&client_id=$clientId&redirect_uri=$redirectUri&code=$code")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .retrieve()
            .bodyToMono(KakaoTokenResponse::class.java)
            .block()!!
        return response.access_token
    }

    fun getUserInfo(accessToken: String): KakaoUserResponse {
        return webClient.get()
            .uri("https://kapi.kakao.com/v2/user/me")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .bodyToMono(KakaoUserResponse::class.java)
            .block()!!
    }
}
```

- [ ] **Step 5: 3개 AuthService 구현 (각 사용자 유형별 분리)**

```kotlin
// auth/service/ParticipantAuthService.kt
package com.blinddate.auth.service

import com.blinddate.auth.dto.TokenResponse
import com.blinddate.auth.jwt.JwtTokenProvider
import com.blinddate.auth.jwt.UserType
import com.blinddate.common.exception.UnauthorizedException
import com.blinddate.participant.entity.Participant
import com.blinddate.participant.repository.ParticipantRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ParticipantAuthService(
    private val participantRepository: ParticipantRepository,
    private val kakaoOAuthService: KakaoOAuthService,
    private val jwtTokenProvider: JwtTokenProvider
) {
    @Transactional
    fun kakaoLogin(code: String): TokenResponse {
        val kakaoToken = kakaoOAuthService.getAccessToken(code)
        val userInfo = kakaoOAuthService.getUserInfo(kakaoToken)
        val kakaoId = userInfo.id.toString()

        val isNew = !participantRepository.existsByKakaoId(kakaoId)
        val participant = participantRepository.findByKakaoId(kakaoId).orElseGet {
            participantRepository.save(Participant(
                kakaoId = kakaoId,
                nickname = userInfo.kakao_account?.profile?.nickname ?: ""
            ))
        }

        return TokenResponse(
            accessToken = jwtTokenProvider.createAccessToken(participant.id, UserType.PARTICIPANT),
            refreshToken = jwtTokenProvider.createRefreshToken(participant.id, UserType.PARTICIPANT),
            isNewUser = isNew
        )
    }

    fun refreshToken(refreshToken: String): TokenResponse {
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw UnauthorizedException("유효하지 않은 리프레시 토큰입니다")
        }
        val principal = jwtTokenProvider.getUserPrincipal(refreshToken)
        return TokenResponse(
            accessToken = jwtTokenProvider.createAccessToken(principal.id, principal.userType, principal.barId),
            refreshToken = jwtTokenProvider.createRefreshToken(principal.id, principal.userType)
        )
    }
}

// auth/service/BarOwnerAuthService.kt
package com.blinddate.auth.service

import com.blinddate.auth.dto.TokenResponse
import com.blinddate.auth.jwt.JwtTokenProvider
import com.blinddate.auth.jwt.UserType
import com.blinddate.barowner.repository.BarOwnerRepository
import com.blinddate.common.exception.UnauthorizedException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class BarOwnerAuthService(
    private val barOwnerRepository: BarOwnerRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder
) {
    fun login(email: String, password: String): TokenResponse {
        val owner = barOwnerRepository.findByEmail(email)
            .orElseThrow { UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다") }
        if (!passwordEncoder.matches(password, owner.password)) {
            throw UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다")
        }
        return TokenResponse(
            accessToken = jwtTokenProvider.createAccessToken(owner.id, UserType.BAR_OWNER, barId = owner.bar.id),
            refreshToken = jwtTokenProvider.createRefreshToken(owner.id, UserType.BAR_OWNER)
        )
    }
}

// auth/service/AdminAuthService.kt
package com.blinddate.auth.service

import com.blinddate.admin.repository.PlatformAdminRepository
import com.blinddate.auth.dto.TokenResponse
import com.blinddate.auth.jwt.JwtTokenProvider
import com.blinddate.auth.jwt.UserType
import com.blinddate.common.exception.UnauthorizedException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AdminAuthService(
    private val platformAdminRepository: PlatformAdminRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder
) {
    fun login(email: String, password: String): TokenResponse {
        val admin = platformAdminRepository.findByEmail(email)
            .orElseThrow { UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다") }
        if (!passwordEncoder.matches(password, admin.password)) {
            throw UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다")
        }
        return TokenResponse(
            accessToken = jwtTokenProvider.createAccessToken(admin.id, UserType.PLATFORM_ADMIN),
            refreshToken = jwtTokenProvider.createRefreshToken(admin.id, UserType.PLATFORM_ADMIN)
        )
    }
}
```

- [ ] **Step 6: 테스트 통과 확인**

Run: `cd backend && ./gradlew test --tests "com.blinddate.auth.service.BarOwnerAuthServiceTest"`
Expected: PASS

- [ ] **Step 7: 컨트롤러 생성 (Auth, BarOwnerAuth, AdminAuth)**

```kotlin
// auth/controller/AuthController.kt
package com.blinddate.auth.controller

import com.blinddate.auth.dto.*
import com.blinddate.auth.service.ParticipantAuthService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(private val participantAuthService: ParticipantAuthService) {

    @PostMapping("/kakao")
    fun kakaoLogin(@RequestBody request: KakaoLoginRequest) = participantAuthService.kakaoLogin(request.code)

    @PostMapping("/refresh")
    fun refresh(@RequestBody request: RefreshRequest) = participantAuthService.refreshToken(request.refreshToken)
}
```

```kotlin
// barowner/controller/BarOwnerAuthController.kt
package com.blinddate.barowner.controller

import com.blinddate.auth.dto.EmailLoginRequest
import com.blinddate.auth.service.BarOwnerAuthService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/bar-owner/auth")
class BarOwnerAuthController(private val barOwnerAuthService: BarOwnerAuthService) {

    @PostMapping("/login")
    fun login(@RequestBody request: EmailLoginRequest) = barOwnerAuthService.login(request.email, request.password)
}
```

```kotlin
// admin/controller/AdminAuthController.kt
package com.blinddate.admin.controller

import com.blinddate.auth.dto.EmailLoginRequest
import com.blinddate.auth.service.AdminAuthService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/auth")
class AdminAuthController(private val adminAuthService: AdminAuthService) {

    @PostMapping("/login")
    fun login(@RequestBody request: EmailLoginRequest) = adminAuthService.login(request.email, request.password)
}
```

- [ ] **Step 8: 커밋**

```bash
git add -A && git commit -m "feat: add auth endpoints (Kakao OAuth, BarOwner login, Admin login)"
```

---

## Task 5: 참가자 API (내 정보 + 프로필)

`GET /api/me`, `POST/PUT /api/me/profile` 엔드포인트를 구현한다.

**Files:**
- Create: `backend/src/main/kotlin/com/blinddate/participant/dto/ParticipantDtos.kt`
- Create: `backend/src/main/kotlin/com/blinddate/participant/service/ParticipantService.kt`
- Create: `backend/src/main/kotlin/com/blinddate/participant/controller/ParticipantController.kt`
- Test: `backend/src/test/kotlin/com/blinddate/participant/service/ParticipantServiceTest.kt`

- [ ] **Step 1: DTOs 생성**

```kotlin
// participant/dto/ParticipantDtos.kt
package com.blinddate.participant.dto

import com.blinddate.participant.entity.*

data class ParticipantResponse(
    val id: Long, val nickname: String, val phoneNumber: String, val isProfileComplete: Boolean
)

data class ProfileCreateRequest(
    val name: String, val age: Int, val gender: Gender, val job: String,
    val height: Int = 0, val mbti: String = "", val hobby: String = "",
    val drinking: DrinkingType = DrinkingType.NONE, val smoking: SmokingType = SmokingType.NONE,
    val religion: String = "", val idealType: String = "", val introduction: String = ""
)

data class ProfileResponse(
    val id: Long, val name: String, val age: Int, val gender: Gender, val job: String,
    val height: Int, val mbti: String, val hobby: String,
    val drinking: DrinkingType, val smoking: SmokingType, val religion: String,
    val idealType: String, val introduction: String, val photoUrl: String
)
```

- [ ] **Step 2: 서비스 테스트 작성**

```kotlin
// test: participant/service/ParticipantServiceTest.kt
package com.blinddate.participant.service

import com.blinddate.common.exception.ConflictException
import com.blinddate.common.exception.NotFoundException
import com.blinddate.participant.dto.ProfileCreateRequest
import com.blinddate.participant.entity.*
import com.blinddate.participant.repository.*
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.Optional

class ParticipantServiceTest {
    private val participantRepo = mockk<ParticipantRepository>()
    private val profileRepo = mockk<ParticipantProfileRepository>()
    private val service = ParticipantService(participantRepo, profileRepo)

    @Test
    fun `getMe should return participant info`() {
        val p = Participant(kakaoId = "123", nickname = "tester", isProfileComplete = true)
        every { participantRepo.findById(1L) } returns Optional.of(p)
        val result = service.getMe(1L)
        assertEquals("tester", result.nickname)
    }

    @Test
    fun `createProfile should save and mark profile complete`() {
        val p = Participant(kakaoId = "123")
        every { participantRepo.findById(1L) } returns Optional.of(p)
        every { profileRepo.existsByParticipantId(1L) } returns false
        every { profileRepo.save(any()) } answers { firstArg() }

        val request = ProfileCreateRequest(name = "홍길동", age = 28, gender = Gender.MALE, job = "개발자")
        val result = service.createProfile(1L, request)
        assertEquals("홍길동", result.name)
        assertTrue(p.isProfileComplete)
    }

    @Test
    fun `createProfile should throw if profile exists`() {
        val p = Participant(kakaoId = "123")
        every { participantRepo.findById(1L) } returns Optional.of(p)
        every { profileRepo.existsByParticipantId(1L) } returns true

        assertThrows(ConflictException::class.java) {
            service.createProfile(1L, ProfileCreateRequest("a", 20, Gender.MALE, "b"))
        }
    }
}
```

- [ ] **Step 3: 테스트 실패 확인**

Run: `cd backend && ./gradlew test --tests "com.blinddate.participant.service.ParticipantServiceTest"`
Expected: FAIL

- [ ] **Step 4: ParticipantService 구현**

```kotlin
// participant/service/ParticipantService.kt
package com.blinddate.participant.service

import com.blinddate.common.exception.*
import com.blinddate.participant.dto.*
import com.blinddate.participant.entity.ParticipantProfile
import com.blinddate.participant.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ParticipantService(
    private val participantRepository: ParticipantRepository,
    private val profileRepository: ParticipantProfileRepository
) {
    fun getMe(participantId: Long): ParticipantResponse {
        val p = participantRepository.findById(participantId)
            .orElseThrow { NotFoundException("참가자를 찾을 수 없습니다") }
        return ParticipantResponse(p.id, p.nickname, p.phoneNumber, p.isProfileComplete)
    }

    @Transactional
    fun createProfile(participantId: Long, request: ProfileCreateRequest): ProfileResponse {
        val p = participantRepository.findById(participantId)
            .orElseThrow { NotFoundException("참가자를 찾을 수 없습니다") }
        if (profileRepository.existsByParticipantId(participantId)) throw ConflictException("프로필이 이미 존재합니다")

        val profile = profileRepository.save(ParticipantProfile(
            participant = p, name = request.name, age = request.age, gender = request.gender,
            job = request.job, height = request.height, mbti = request.mbti, hobby = request.hobby,
            drinking = request.drinking, smoking = request.smoking, religion = request.religion,
            idealType = request.idealType, introduction = request.introduction
        ))
        p.isProfileComplete = true
        return profile.toResponse()
    }

    fun getProfile(participantId: Long): ProfileResponse =
        profileRepository.findByParticipantId(participantId)
            .orElseThrow { NotFoundException("프로필을 찾을 수 없습니다") }.toResponse()

    @Transactional
    fun updateProfile(participantId: Long, request: ProfileCreateRequest): ProfileResponse {
        val profile = profileRepository.findByParticipantId(participantId)
            .orElseThrow { NotFoundException("프로필을 찾을 수 없습니다") }
        profile.apply {
            name = request.name; age = request.age; gender = request.gender; job = request.job
            height = request.height; mbti = request.mbti; hobby = request.hobby
            drinking = request.drinking; smoking = request.smoking; religion = request.religion
            idealType = request.idealType; introduction = request.introduction
        }
        return profile.toResponse()
    }

    private fun ParticipantProfile.toResponse() = ProfileResponse(
        id, name, age, gender, job, height, mbti, hobby, drinking, smoking, religion, idealType, introduction, photoUrl
    )
}
```

- [ ] **Step 5: 테스트 통과 확인**

Run: `cd backend && ./gradlew test --tests "com.blinddate.participant.service.ParticipantServiceTest"`
Expected: PASS

- [ ] **Step 6: ParticipantController 구현**

```kotlin
// participant/controller/ParticipantController.kt
package com.blinddate.participant.controller

import com.blinddate.auth.jwt.UserPrincipal
import com.blinddate.participant.dto.ProfileCreateRequest
import com.blinddate.participant.service.ParticipantService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/me")
class ParticipantController(private val service: ParticipantService) {

    @GetMapping
    fun getMe(@AuthenticationPrincipal principal: UserPrincipal) = service.getMe(principal.id)

    @GetMapping("/profile")
    fun getProfile(@AuthenticationPrincipal principal: UserPrincipal) = service.getProfile(principal.id)

    @PostMapping("/profile")
    fun createProfile(@AuthenticationPrincipal principal: UserPrincipal, @RequestBody request: ProfileCreateRequest) =
        service.createProfile(principal.id, request)

    @PutMapping("/profile")
    fun updateProfile(@AuthenticationPrincipal principal: UserPrincipal, @RequestBody request: ProfileCreateRequest) =
        service.updateProfile(principal.id, request)
}
```

- [ ] **Step 7: 커밋**

```bash
git add -A && git commit -m "feat: add participant API (me, profile CRUD)"
```

---

## Task 6: Event 엔티티 + 공개 API (바 정보 + 이벤트 목록)

Event 엔티티를 생성하고, 참가자가 바별 페이지에서 이벤트 목록을 확인하는 공개 API를 구현한다.

**Files:**
- Create: `backend/src/main/kotlin/com/blinddate/event/entity/Event.kt`
- Create: `backend/src/main/kotlin/com/blinddate/event/entity/EventStatus.kt`
- Create: `backend/src/main/kotlin/com/blinddate/event/entity/MatchingMode.kt`
- Create: `backend/src/main/kotlin/com/blinddate/event/repository/EventRepository.kt`
- Create: `backend/src/main/kotlin/com/blinddate/event/dto/EventDtos.kt`
- Create: `backend/src/main/kotlin/com/blinddate/event/service/EventService.kt`
- Create: `backend/src/main/kotlin/com/blinddate/event/controller/EventController.kt`
- Create: `backend/src/main/kotlin/com/blinddate/bar/dto/BarDtos.kt`
- Create: `backend/src/main/kotlin/com/blinddate/bar/service/BarService.kt`
- Create: `backend/src/main/kotlin/com/blinddate/bar/controller/BarController.kt`
- Test: `backend/src/test/kotlin/com/blinddate/event/service/EventServiceTest.kt`

- [ ] **Step 1: Event 관련 엔티티/enum 생성**

```kotlin
// event/entity/EventStatus.kt
package com.blinddate.event.entity
enum class EventStatus { OPEN, CLOSED, COMPLETED }

// event/entity/MatchingMode.kt
package com.blinddate.event.entity
enum class MatchingMode { BIDIRECTIONAL }
```

```kotlin
// event/entity/Event.kt
package com.blinddate.event.entity

import com.blinddate.bar.entity.Bar
import com.blinddate.common.entity.BaseEntity
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity
@Table(name = "event")
class Event(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bar_id", nullable = false)
    val bar: Bar,

    @Column(nullable = false) var title: String,
    @Column(nullable = false) var date: LocalDate,
    @Column(nullable = false) var time: LocalTime,
    @Column(nullable = false) var price: Int,
    @Column(nullable = false) var maleCapacity: Int,
    @Column(nullable = false) var femaleCapacity: Int,
    @Column(nullable = false) var currentMaleCount: Int = 0,
    @Column(nullable = false) var currentFemaleCount: Int = 0,
    @Column(columnDefinition = "TEXT") var description: String = "",
    var choiceDeadline: LocalDateTime? = null,
    var matchNotificationTime: LocalDateTime? = null,
    var minAge: Int? = null,
    var maxAge: Int? = null,
    @Column(nullable = false) var maxChoices: Int = 3,
    @Enumerated(EnumType.STRING) @Column(nullable = false) var matchingMode: MatchingMode = MatchingMode.BIDIRECTIONAL,
    @Enumerated(EnumType.STRING) @Column(nullable = false) var status: EventStatus = EventStatus.OPEN,
    var deletedAt: LocalDateTime? = null
) : BaseEntity() {
    @Version
    var version: Long = 0  // 낙관적 잠금 - 동시 승인 시 정원 초과 방지

    fun isDeleted() = deletedAt != null
    fun hasAvailableMaleSlots() = currentMaleCount < maleCapacity
    fun hasAvailableFemaleSlots() = currentFemaleCount < femaleCapacity
}
```

- [ ] **Step 2: EventRepository + DTOs 생성**

```kotlin
// event/repository/EventRepository.kt
package com.blinddate.event.repository
import com.blinddate.event.entity.Event
import com.blinddate.event.entity.EventStatus
import org.springframework.data.jpa.repository.JpaRepository
interface EventRepository : JpaRepository<Event, Long> {
    fun findByBarIdAndDeletedAtIsNullOrderByDateAsc(barId: Long): List<Event>
    fun findByBarIdAndStatusAndDeletedAtIsNull(barId: Long, status: EventStatus): List<Event>
    fun findByStatus(status: EventStatus): List<Event>
}
```

```kotlin
// event/dto/EventDtos.kt
package com.blinddate.event.dto
import com.blinddate.event.entity.EventStatus
import com.blinddate.event.entity.MatchingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class EventResponse(
    val id: Long, val barId: Long, val title: String, val date: LocalDate, val time: LocalTime,
    val maleCapacity: Int, val femaleCapacity: Int, val currentMaleCount: Int, val currentFemaleCount: Int,
    val price: Int, val status: EventStatus, val description: String,
    val choiceDeadline: LocalDateTime?, val matchNotificationTime: LocalDateTime?,
    val minAge: Int?, val maxAge: Int?, val maxChoices: Int, val matchingMode: MatchingMode
)

data class EventCreateRequest(
    val title: String, val date: LocalDate, val time: LocalTime,
    val maleCapacity: Int, val femaleCapacity: Int, val price: Int,
    val description: String = "", val choiceDeadline: LocalDateTime? = null,
    val matchNotificationTime: LocalDateTime? = null, val minAge: Int? = null,
    val maxAge: Int? = null, val maxChoices: Int = 3, val matchingMode: MatchingMode = MatchingMode.BIDIRECTIONAL
)
```

- [ ] **Step 3: EventService 테스트 작성**

```kotlin
// test: event/service/EventServiceTest.kt
package com.blinddate.event.service

import com.blinddate.bar.entity.Bar
import com.blinddate.bar.repository.BarRepository
import com.blinddate.event.entity.Event
import com.blinddate.event.entity.EventStatus
import com.blinddate.event.repository.EventRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime

class EventServiceTest {
    private val eventRepo = mockk<EventRepository>()
    private val barRepo = mockk<BarRepository>()
    private val service = EventService(eventRepo, barRepo)

    @Test
    fun `getEventsByBar should return events for given bar slug`() {
        val bar = Bar(name = "Test", address = "addr", slug = "test-bar")
        val event = Event(bar = bar, title = "Friday", date = LocalDate.of(2026, 4, 1),
            time = LocalTime.of(19, 0), price = 30000, maleCapacity = 10, femaleCapacity = 10)

        every { barRepo.findBySlug("test-bar") } returns java.util.Optional.of(bar)
        every { eventRepo.findByBarIdAndDeletedAtIsNullOrderByDateAsc(bar.id) } returns listOf(event)

        val result = service.getEventsByBar("test-bar")
        assertEquals(1, result.size)
        assertEquals("Friday", result[0].title)
    }
}
```

- [ ] **Step 4: 테스트 실패 확인 → EventService + BarService 구현 → 테스트 통과**

Run: `cd backend && ./gradlew test --tests "com.blinddate.event.service.EventServiceTest"`

```kotlin
// event/service/EventService.kt
package com.blinddate.event.service

import com.blinddate.bar.repository.BarRepository
import com.blinddate.common.exception.NotFoundException
import com.blinddate.event.dto.*
import com.blinddate.event.entity.Event
import com.blinddate.event.repository.EventRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class EventService(
    private val eventRepository: EventRepository,
    private val barRepository: BarRepository
) {
    fun getEventsByBar(slug: String): List<EventResponse> {
        val bar = barRepository.findBySlug(slug).orElseThrow { NotFoundException("바를 찾을 수 없습니다") }
        return eventRepository.findByBarIdAndDeletedAtIsNullOrderByDateAsc(bar.id).map { it.toResponse() }
    }

    fun getEvent(eventId: Long): EventResponse {
        val event = eventRepository.findById(eventId).orElseThrow { NotFoundException("이벤트를 찾을 수 없습니다") }
        if (event.isDeleted()) throw NotFoundException("삭제된 이벤트입니다")
        return event.toResponse()
    }

    fun Event.toResponse() = EventResponse(
        id, bar.id, title, date, time, maleCapacity, femaleCapacity,
        currentMaleCount, currentFemaleCount, price, status, description,
        choiceDeadline, matchNotificationTime, minAge, maxAge, maxChoices, matchingMode
    )
}
```

```kotlin
// bar/dto/BarDtos.kt
package com.blinddate.bar.dto
data class BarResponse(
    val id: Long, val name: String, val address: String, val description: String,
    val logoUrl: String, val coverImageUrl: String, val slug: String, val isActive: Boolean
)

// bar/service/BarService.kt
package com.blinddate.bar.service
import com.blinddate.bar.dto.BarResponse
import com.blinddate.bar.entity.Bar
import com.blinddate.bar.repository.BarRepository
import com.blinddate.common.exception.NotFoundException
import org.springframework.stereotype.Service
@Service
class BarService(private val barRepository: BarRepository) {
    fun getBySlug(slug: String): BarResponse {
        val bar = barRepository.findBySlug(slug).orElseThrow { NotFoundException("바를 찾을 수 없습니다") }
        return bar.toResponse()
    }
    fun Bar.toResponse() = BarResponse(id, name, address, description, logoUrl, coverImageUrl, slug, isActive)
}

// bar/controller/BarController.kt
package com.blinddate.bar.controller
import com.blinddate.bar.service.BarService
import com.blinddate.event.service.EventService
import org.springframework.web.bind.annotation.*
@RestController
@RequestMapping("/api/bars")
class BarController(private val barService: BarService, private val eventService: EventService) {
    @GetMapping("/{slug}")
    fun getBar(@PathVariable slug: String) = barService.getBySlug(slug)
    @GetMapping("/{slug}/events")
    fun getEvents(@PathVariable slug: String) = eventService.getEventsByBar(slug)
    @GetMapping("/{slug}/events/{eventId}")
    fun getEvent(@PathVariable slug: String, @PathVariable eventId: Long) =
        eventService.getEventByBarSlugAndId(slug, eventId)  // slug 소속 검증 포함
}
```

- [ ] **Step 5: 테스트 통과 확인 → 커밋**

Run: `cd backend && ./gradlew test --tests "com.blinddate.event.service.EventServiceTest"`
Expected: PASS

```bash
git add -A && git commit -m "feat: add Event entity and public bar/event APIs"
```

---

## Task 7: BarOwner 이벤트 CRUD + 바 관리 API

바 사장님이 이벤트를 생성/수정/삭제/조회하고, 자기 바 정보를 수정하는 API를 구현한다.

**Files:**
- Create: `backend/src/main/kotlin/com/blinddate/barowner/dto/BarOwnerDtos.kt`
- Create: `backend/src/main/kotlin/com/blinddate/barowner/service/BarOwnerService.kt`
- Create: `backend/src/main/kotlin/com/blinddate/barowner/controller/BarOwnerBarController.kt`
- Create: `backend/src/main/kotlin/com/blinddate/barowner/controller/BarOwnerEventController.kt`
- Test: `backend/src/test/kotlin/com/blinddate/barowner/service/BarOwnerServiceTest.kt`

- [ ] **Step 1: BarOwnerDtos 생성**

```kotlin
// barowner/dto/BarOwnerDtos.kt
data class BarUpdateRequest(val name: String, val address: String, val description: String)
data class EventStatsResponse(
    val eventId: Long, val title: String, val participantCount: Int,
    val maleCount: Int, val femaleCount: Int, val matchCount: Int
)
```

- [ ] **Step 2: BarOwnerService 테스트 작성 (이벤트 CRUD: 생성, 수정, 삭제, 타 바 이벤트 접근 거부)**
- [ ] **Step 3: 테스트 실패 확인**
- [ ] **Step 4: BarOwnerService 구현**

핵심 로직:
- 모든 이벤트 조작 시 `event.bar.id == principal.barId` 검증
- `createEvent`: Event 생성, bar 연결
- `updateEvent`: 소유권 검증 후 수정
- `deleteEvent`: soft delete (deletedAt 설정)
- `closeEvent`: 수동 OPEN → CLOSED 전환
- `getEventStats`: 참가자 수, 매칭 수 통계
- `getMyCommissions`: 내 바의 수수료 내역 조회

- [ ] **Step 5: 테스트 통과 확인**
- [ ] **Step 6: 컨트롤러 구현**

```kotlin
// BarOwnerBarController: GET/PUT /api/bar-owner/my-bar
// BarOwnerEventController: CRUD /api/bar-owner/events, GET .../stats
//   + PUT /api/bar-owner/events/{id}/close (수동 마감)
//   + GET /api/bar-owner/commissions
```

- [ ] **Step 7: 커밋**

```bash
git commit -m "feat: add BarOwner event CRUD, bar management, stats, and commissions APIs"
```

---

## Task 8: Application 엔티티 + 참가자 신청/취소

참가자가 이벤트에 신청하고 취소하는 기능을 구현한다.

**Files:**
- Create: `backend/src/main/kotlin/com/blinddate/application/entity/Application.kt`
- Create: `backend/src/main/kotlin/com/blinddate/application/entity/ApplicationStatus.kt`
- Create: `backend/src/main/kotlin/com/blinddate/application/repository/ApplicationRepository.kt`
- Create: `backend/src/main/kotlin/com/blinddate/application/dto/ApplicationDtos.kt`
- Create: `backend/src/main/kotlin/com/blinddate/application/service/ApplicationService.kt`
- Create: `backend/src/main/kotlin/com/blinddate/application/controller/ApplicationController.kt`
- Test: `backend/src/test/kotlin/com/blinddate/application/service/ApplicationServiceTest.kt`

- [ ] **Step 1: ApplicationStatus enum + Application 엔티티**

```kotlin
// application/entity/ApplicationStatus.kt
package com.blinddate.application.entity
enum class ApplicationStatus { PENDING, APPROVED, REJECTED, CANCELLED, COMPLETED }
```

```kotlin
// application/entity/Application.kt
package com.blinddate.application.entity

import com.blinddate.barowner.entity.BarOwner
import com.blinddate.common.entity.BaseEntity
import com.blinddate.event.entity.Event
import com.blinddate.participant.entity.Participant
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "application",
    uniqueConstraints = [UniqueConstraint(columnNames = ["participant_id", "event_id"])])
class Application(
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "participant_id", nullable = false)
    val participant: Participant,

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "event_id", nullable = false)
    val event: Event,

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    var status: ApplicationStatus = ApplicationStatus.PENDING,

    @Column(nullable = false) var appliedAt: LocalDateTime = LocalDateTime.now(),
    var reviewedAt: LocalDateTime? = null,

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "reviewed_by")
    var reviewedBy: BarOwner? = null,

    @Column(columnDefinition = "TEXT") var rejectReason: String? = null
) : BaseEntity()
```

- [ ] **Step 2: Repository + DTOs**
- [ ] **Step 3: ApplicationService 테스트 작성 (신청: 정상, 중복신청 에러, OPEN이 아닌 이벤트 에러, 나이제한 에러)**
- [ ] **Step 4: 테스트 실패 확인**
- [ ] **Step 5: ApplicationService 구현 (apply, cancel, getMyApplications)**

핵심 로직:
- 프로필 필수 검증
- 이벤트 OPEN 상태 검증
- 나이 제한 검증
- 중복 신청 방지 (unique constraint)
- 취소는 PENDING 상태만 가능

- [ ] **Step 6: 테스트 통과 확인**
- [ ] **Step 7: ApplicationController 구현**

```kotlin
// POST /api/events/{id}/apply
// DELETE /api/events/{id}/apply
// GET /api/me/applications
```

- [ ] **Step 8: 커밋**

```bash
git commit -m "feat: add application flow (participant apply/cancel)"
```

---

## Task 9: BarOwner 신청 관리 (승인/거절 + 정원 관리)

바 사장님이 신청자를 승인/거절하고, 승인 시 이벤트 정원 카운트를 관리하는 기능을 구현한다.

**Files:**
- Create: `backend/src/main/kotlin/com/blinddate/barowner/controller/BarOwnerApplicationController.kt`
- Modify: `backend/src/main/kotlin/com/blinddate/application/service/ApplicationService.kt`
- Test: 기존 ApplicationServiceTest에 추가

- [ ] **Step 1: 승인/거절 테스트 작성 (승인→카운트 증가, 정원 초과 에러, 거절)**
- [ ] **Step 2: 테스트 실패 확인**
- [ ] **Step 3: ApplicationService에 approve/reject 구현**

핵심 로직:
- 승인 시 PENDING → APPROVED, 해당 성별 카운트 증가
- 정원 초과 시 에러
- 거절 시 PENDING → REJECTED (카운트 변동 없음)
- barOwnerId로 해당 바의 이벤트인지 검증

- [ ] **Step 4: 테스트 통과 확인**
- [ ] **Step 5: BarOwnerApplicationController 구현**
- [ ] **Step 6: 커밋**

```bash
git commit -m "feat: add BarOwner application management (approve/reject with capacity)"
```

---

## Task 10: 참가번호 부여 + 선택(Choice) 제출

이벤트가 CLOSED되면 참가번호를 부여하고, 참가자가 이성을 선택하는 기능을 구현한다.

**Files:**
- Create: `backend/src/main/kotlin/com/blinddate/matching/entity/ParticipantNumber.kt`
- Create: `backend/src/main/kotlin/com/blinddate/matching/entity/Choice.kt`
- Create: `backend/src/main/kotlin/com/blinddate/matching/repository/ParticipantNumberRepository.kt`
- Create: `backend/src/main/kotlin/com/blinddate/matching/repository/ChoiceRepository.kt`
- Create: `backend/src/main/kotlin/com/blinddate/matching/dto/MatchingDtos.kt`
- Create: `backend/src/main/kotlin/com/blinddate/matching/service/ParticipantNumberService.kt`
- Create: `backend/src/main/kotlin/com/blinddate/matching/service/MatchingService.kt`
- Create: `backend/src/main/kotlin/com/blinddate/matching/controller/MatchingController.kt`
- Test: `backend/src/test/kotlin/com/blinddate/matching/service/ParticipantNumberServiceTest.kt`
- Test: `backend/src/test/kotlin/com/blinddate/matching/service/MatchingServiceTest.kt`

- [ ] **Step 1: ParticipantNumber + Choice 엔티티**
- [ ] **Step 2: ParticipantNumberService 테스트 (번호 부여: 남녀 분리, 순차)**
- [ ] **Step 3: 테스트 실패 확인 → 구현 → 통과**
- [ ] **Step 4: MatchingService 테스트 (선택 제출: 정상, maxChoices 초과, 마감 후 에러, 이성만 선택 가능)**
- [ ] **Step 5: 테스트 실패 확인 → 구현 → 통과**

핵심 로직:
- `getParticipants`: 이성 참가번호 + 나이/직업/한줄소개 반환
- `submitChoices`: choiceDeadline 검증, maxChoices 검증, 이성 검증
- `updateChoices`: 기존 선택 삭제 후 재저장

- [ ] **Step 6: MatchingController 구현**
- [ ] **Step 7: 커밋**

```bash
git commit -m "feat: add participant numbers and choice submission"
```

---

## Task 11: 매칭 알고리즘 + 결과 조회

양방향 매칭 알고리즘을 구현하고, 매칭 결과를 조회하는 API를 추가한다.

**Files:**
- Create: `backend/src/main/kotlin/com/blinddate/matching/entity/MatchResult.kt`
- Create: `backend/src/main/kotlin/com/blinddate/matching/repository/MatchResultRepository.kt`
- Modify: `backend/src/main/kotlin/com/blinddate/matching/service/MatchingService.kt`
- Modify: `backend/src/main/kotlin/com/blinddate/matching/controller/MatchingController.kt`

- [ ] **Step 1: MatchResult 엔티티 + Repository**
- [ ] **Step 2: 매칭 알고리즘 테스트 (상호 선택→매칭, 일방 선택→미매칭, 중복 매칭 방지)**

```kotlin
@Test
fun `processMatching should match bidirectional choices`() {
    // A가 B를 선택, B가 A를 선택 → 매칭 성사
    // A가 C를 선택, C는 A 미선택 → 매칭 불성립
}
```

- [ ] **Step 3: 테스트 실패 확인 → processMatching 구현 → 통과**
- [ ] **Step 4: getMatchResult 구현 (매칭 결과 + 상대 닉네임)**
- [ ] **Step 5: 컨트롤러에 `GET /api/events/{id}/result` 추가**
- [ ] **Step 6: 커밋**

```bash
git commit -m "feat: add bidirectional matching algorithm and result query"
```

---

## Task 12: 알림 시스템 (DB 저장 + 조회)

알림 엔티티와 서비스를 구현한다. 카카오 채널 메시지 연동은 Task 16에서 별도 처리.

**Files:**
- Create: `backend/src/main/kotlin/com/blinddate/notification/entity/RecipientType.kt`
- Create: `backend/src/main/kotlin/com/blinddate/notification/entity/NotificationType.kt`
- Create: `backend/src/main/kotlin/com/blinddate/notification/entity/Notification.kt`
- Create: `backend/src/main/kotlin/com/blinddate/notification/repository/NotificationRepository.kt`
- Create: `backend/src/main/kotlin/com/blinddate/notification/dto/NotificationDtos.kt`
- Create: `backend/src/main/kotlin/com/blinddate/notification/service/NotificationService.kt`
- Create: `backend/src/main/kotlin/com/blinddate/notification/controller/NotificationController.kt`
- Test: `backend/src/test/kotlin/com/blinddate/notification/service/NotificationServiceTest.kt`

- [ ] **Step 1: Enum + Notification 엔티티**

```kotlin
// notification/entity/RecipientType.kt
package com.blinddate.notification.entity
enum class RecipientType { PARTICIPANT, BAR_OWNER }

// notification/entity/NotificationType.kt
package com.blinddate.notification.entity
enum class NotificationType {
    NEW_APPLICATION, APPROVED, REJECTED, MATCH_RESULT,
    EVENT_REMINDER, CHOICE_REMINDER, EVENT_COMPLETED, COMMISSION_INVOICE
}
```

```kotlin
// notification/entity/Notification.kt
package com.blinddate.notification.entity
import com.blinddate.common.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "notification")
class Notification(
    @Enumerated(EnumType.STRING) @Column(nullable = false) val recipientType: RecipientType,
    @Column(nullable = false) val recipientId: Long,
    @Enumerated(EnumType.STRING) @Column(nullable = false) val type: NotificationType,
    @Column(nullable = false) val title: String,
    @Column(columnDefinition = "TEXT", nullable = false) val message: String,
    @Column(nullable = false) var isRead: Boolean = false
) : BaseEntity()
```

- [ ] **Step 2: Repository + DTOs + 테스트 작성**
- [ ] **Step 3: 테스트 실패 → NotificationService 구현 (send, getNotifications, markAsRead, getUnreadCount) → 통과**
- [ ] **Step 4: NotificationController 구현**
- [ ] **Step 5: 커밋**

```bash
git commit -m "feat: add notification system (DB storage and retrieval)"
```

---

## Task 13: ActionToken 시스템 (카톡 버튼 액션)

바 사장님이 카톡 메시지의 버튼을 클릭하여 신청을 승인/거절할 수 있는 일회용 토큰 시스템을 구현한다.

**Files:**
- Create: `backend/src/main/kotlin/com/blinddate/action/entity/ActionToken.kt`
- Create: `backend/src/main/kotlin/com/blinddate/action/repository/ActionTokenRepository.kt`
- Create: `backend/src/main/kotlin/com/blinddate/action/dto/ActionDtos.kt`
- Create: `backend/src/main/kotlin/com/blinddate/action/service/ActionTokenService.kt`
- Create: `backend/src/main/kotlin/com/blinddate/action/controller/ActionController.kt`
- Test: `backend/src/test/kotlin/com/blinddate/action/service/ActionTokenServiceTest.kt`

- [ ] **Step 1: ActionToken 엔티티**

```kotlin
// action/entity/ActionToken.kt
package com.blinddate.action.entity
import com.blinddate.common.entity.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "action_token")
class ActionToken(
    @Column(unique = true, nullable = false)
    val token: String = UUID.randomUUID().toString(),

    @Column(nullable = false) val actionType: String,
    @Column(nullable = false) val targetId: Long,
    @Column(nullable = false) val barOwnerId: Long,
    @Column(nullable = false) var used: Boolean = false,
    @Column(nullable = false) val expiresAt: LocalDateTime = LocalDateTime.now().plusHours(24)
) : BaseEntity() {
    fun isExpired() = LocalDateTime.now().isAfter(expiresAt)
    fun isValid() = !used && !isExpired()
}
```

- [ ] **Step 2: 테스트 작성 (토큰 생성, 실행, 만료 토큰 거부, 사용 완료 토큰 거부)**
- [ ] **Step 3: 테스트 실패 → ActionTokenService 구현 → 통과**

핵심 로직:
- `createToken`: 토큰 생성 + DB 저장
- `executeAction`: 유효성 검증 → ApplicationService.approve/reject 호출 → used=true (atomic)
- UUID v4 토큰, 24시간 만료, 1회용

- [ ] **Step 4: ActionController 구현 (`GET/POST /api/actions/{token}`)**
- [ ] **Step 5: 커밋**

```bash
git commit -m "feat: add ActionToken system for KakaoTalk button actions"
```

---

## Task 14: Commission 시스템 (수수료 추적)

이벤트 완료 시 자동으로 수수료 레코드를 생성하고, 관리자가 청구/입금 확인하는 기능을 구현한다.

**Files:**
- Create: `backend/src/main/kotlin/com/blinddate/commission/entity/Commission.kt`
- Create: `backend/src/main/kotlin/com/blinddate/commission/entity/CommissionStatus.kt`
- Create: `backend/src/main/kotlin/com/blinddate/commission/repository/CommissionRepository.kt`
- Create: `backend/src/main/kotlin/com/blinddate/commission/dto/CommissionDtos.kt`
- Create: `backend/src/main/kotlin/com/blinddate/commission/service/CommissionService.kt`
- Test: `backend/src/test/kotlin/com/blinddate/commission/service/CommissionServiceTest.kt`

- [ ] **Step 1: Commission 엔티티 + enum + repository**

```kotlin
// commission/entity/CommissionStatus.kt
package com.blinddate.commission.entity
enum class CommissionStatus { PENDING, INVOICED, PAID }
```

```kotlin
// commission/entity/Commission.kt
package com.blinddate.commission.entity
import com.blinddate.bar.entity.Bar
import com.blinddate.common.entity.BaseEntity
import com.blinddate.event.entity.Event
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "commission")
class Commission(
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "bar_id", nullable = false) val bar: Bar,
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "event_id", unique = true, nullable = false) val event: Event,
    @Column(nullable = false) val participantCount: Int,
    @Column(nullable = false) val eventPrice: Int,
    @Column(nullable = false) val commissionRate: Int,
    @Column(nullable = false) val unitPrice: Int,
    @Column(nullable = false) val totalAmount: Int,
    @Enumerated(EnumType.STRING) @Column(nullable = false) var status: CommissionStatus = CommissionStatus.PENDING,
    var invoicedAt: LocalDateTime? = null,
    var paidAt: LocalDateTime? = null
) : BaseEntity()
```

- [ ] **Step 2: 테스트 (자동 생성: 참가자 수 × 단가 계산, 청구 상태 전환, 입금 확인)**
- [ ] **Step 3: 테스트 실패 → CommissionService 구현 → 통과**

핵심 로직:
- `createForEvent(eventId)`: APPROVED 이상 참가자 수 계산 → unitPrice = eventPrice * commissionRate / 100 → 저장
- `invoice(commissionId)`: PENDING → INVOICED
- `markPaid(commissionId)`: INVOICED → PAID

- [ ] **Step 4: 커밋**

```bash
git commit -m "feat: add commission system (auto-creation, invoice, payment tracking)"
```

---

## Task 15: 플랫폼 관리자 API (바 등록, 수수료 관리, 대시보드)

플랫폼 관리자가 바를 등록하고, 수수료를 관리하고, 전체 통계를 확인하는 API를 구현한다.

**Files:**
- Create: `backend/src/main/kotlin/com/blinddate/admin/dto/AdminDtos.kt`
- Create: `backend/src/main/kotlin/com/blinddate/admin/service/AdminService.kt`
- Create: `backend/src/main/kotlin/com/blinddate/admin/controller/AdminBarController.kt`
- Create: `backend/src/main/kotlin/com/blinddate/admin/controller/AdminCommissionController.kt`
- Create: `backend/src/main/kotlin/com/blinddate/admin/controller/AdminDashboardController.kt`
- Test: `backend/src/test/kotlin/com/blinddate/admin/service/AdminServiceTest.kt`

- [ ] **Step 1: AdminDtos + 테스트**

```kotlin
// admin/dto/AdminDtos.kt - 바 등록 요청, 사장님 계정 생성 요청, 대시보드 응답 등
```

- [ ] **Step 2: AdminService 구현**

핵심 기능:
- `createBar(request)`: Bar 엔티티 생성 (slug 중복 검증)
- `createBarOwner(request)`: BarOwner 생성 (비밀번호 BCrypt 해싱)
- `getBars()`: 바 목록
- `getDashboard()`: 전체 바 수, 이벤트 수, 참가자 수, 수수료 현황

- [ ] **Step 3: 테스트 통과 확인**
- [ ] **Step 4: 컨트롤러 3개 구현**
- [ ] **Step 5: 커밋**

```bash
git commit -m "feat: add platform admin APIs (bar registration, commission, dashboard)"
```

---

## Task 16: 스케줄러 (이벤트 상태 전환 + 매칭 + 리마인더)

이벤트 상태를 자동으로 전환하고, 매칭을 실행하고, 알림을 발송하는 스케줄러를 구현한다.

**Files:**
- Create: `backend/src/main/kotlin/com/blinddate/scheduler/EventStatusScheduler.kt`
- Create: `backend/src/main/kotlin/com/blinddate/scheduler/MatchingScheduler.kt`
- Create: `backend/src/main/kotlin/com/blinddate/scheduler/ReminderScheduler.kt`
- Test: `backend/src/test/kotlin/com/blinddate/scheduler/EventStatusSchedulerTest.kt`
- Test: `backend/src/test/kotlin/com/blinddate/scheduler/MatchingSchedulerTest.kt`

- [ ] **Step 1: EventStatusScheduler 테스트**

```kotlin
@Test
fun `should transition OPEN to CLOSED when choiceDeadline passed`() { ... }

@Test
fun `should assign participant numbers on CLOSED transition`() { ... }
```

- [ ] **Step 2: 테스트 실패 → EventStatusScheduler 구현 → 통과**

핵심 로직:
- 매분 실행: OPEN 이벤트 중 choiceDeadline 지난 것 → CLOSED + 참가번호 부여
- CLOSED 이벤트는 참가자가 선택할 시간이 필요함 → matchNotificationTime까지 대기
- matchNotificationTime 도달한 CLOSED 이벤트 → 매칭 처리 + 알림 발송 → COMPLETED + Commission 생성
- 시간 흐름: choiceDeadline(선택 마감) → [참가자 선택 기간] → matchNotificationTime(매칭 실행)

- [ ] **Step 3: MatchingScheduler 테스트**

```kotlin
@Test
fun `should process matching for CLOSED events`() { ... }

@Test
fun `should send notifications when matchNotificationTime passed`() { ... }

@Test
fun `should notify unmatched participants`() { ... }
```

- [ ] **Step 4: 테스트 실패 → MatchingScheduler 구현 → 통과**
- [ ] **Step 5: ReminderScheduler 구현 (이벤트 전날 리마인더)**
- [ ] **Step 6: 커밋**

```bash
git commit -m "feat: add schedulers (event status, matching, reminder)"
```

---

## Task 17: 카카오 채널 메시지 연동

알림 발송 시 카카오 채널 메시지 API를 호출하여 카톡 메시지를 보내는 기능을 구현한다.

**Files:**
- Create: `backend/src/main/kotlin/com/blinddate/notification/service/KakaoChannelMessageService.kt`
- Modify: `backend/src/main/kotlin/com/blinddate/notification/service/NotificationService.kt`
- Modify: `backend/src/main/resources/application-local.yml` (카카오 채널 설정 추가)

- [ ] **Step 1: KakaoChannelMessageService 구현**

```kotlin
// 카카오 채널 메시지 API 호출 (WebClient)
// 실패 시 로그만 남기고 진행 (알림 DB 저장은 이미 완료)
```

- [ ] **Step 2: NotificationService.send()에서 KakaoChannelMessageService 호출 추가**
- [ ] **Step 3: 바 사장님 알림에 ActionToken URL 버튼 포함**
- [ ] **Step 4: 커밋**

```bash
git commit -m "feat: integrate Kakao channel message API for notifications"
```

---

## Task 18: 파일 업로드 (S3 Presigned URL)

프로필 사진, 바 로고/커버 이미지 업로드를 위한 S3 presigned URL 발급 API를 구현한다.

**Files:**
- Create: `backend/src/main/kotlin/com/blinddate/common/service/S3Service.kt`
- Create: `backend/src/main/kotlin/com/blinddate/common/controller/UploadController.kt`

- [ ] **Step 1: S3Service 구현 (presigned URL 생성)**
- [ ] **Step 2: UploadController 구현 (`POST /api/upload/presigned-url`)**
- [ ] **Step 3: 커밋**

```bash
git commit -m "feat: add S3 presigned URL upload endpoint"
```

---

## Task 19: 전체 통합 테스트 + 정리

빌드, 전체 테스트 실행, 코드 정리를 수행한다.

- [ ] **Step 1: 전체 빌드 확인**

Run: `cd backend && ./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: 전체 테스트 실행**

Run: `cd backend && ./gradlew test`
Expected: 모든 테스트 PASS

- [ ] **Step 3: application 설정 파일 정리 (local/test 환경 분리 확인)**
- [ ] **Step 4: docker-compose.yml 업데이트 (불필요한 환경변수 제거)**
- [ ] **Step 5: 최종 커밋**

```bash
git commit -m "chore: cleanup and verify all tests pass"
```

---

## 구현 순서 의존성

```
Task 1 (공통 인프라)
  └─▶ Task 2 (핵심 엔티티)
       └─▶ Task 3 (JWT 인증)
            └─▶ Task 4 (인증 엔드포인트)
                 ├─▶ Task 5 (참가자 API)
                 ├─▶ Task 6 (Event + 공개 API)
                 │    └─▶ Task 7 (BarOwner 이벤트 CRUD)
                 │         └─▶ Task 8 (신청/취소)
                 │              └─▶ Task 9 (승인/거절)
                 │                   └─▶ Task 10 (참가번호 + 선택)
                 │                        └─▶ Task 11 (매칭 알고리즘)
                 ├─▶ Task 12 (알림 시스템)
                 │    └─▶ Task 13 (ActionToken)
                 │         └─▶ Task 17 (카카오 채널 연동)
                 ├─▶ Task 14 (Commission)
                 │    └─▶ Task 15 (관리자 API)
                 └─▶ Task 18 (파일 업로드)

Task 9 + 11 + 12 + 13 + 14
  └─▶ Task 16 (스케줄러)

All Tasks
  └─▶ Task 19 (통합 테스트)
```

병렬 실행 가능한 그룹:
- **그룹 A** (Task 5~11): 핵심 비즈니스 플로우 (순차)
- **그룹 B** (Task 12~13, 17): 알림 시스템 (Task 4 완료 후 병렬)
- **그룹 C** (Task 14~15): Commission (Task 4 완료 후 병렬)
- **그룹 D** (Task 18): 파일 업로드 (Task 4 완료 후 병렬)
