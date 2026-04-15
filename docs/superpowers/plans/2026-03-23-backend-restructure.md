# Backend Restructure - 역할 분리 구현 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 기존 BarOwner 중심 구조를 CafeOwner(장소 제공) + Organizer(이벤트 운영)로 분리하고, 모든 Bar→Cafe 리네이밍을 완료한다.

**Architecture:** 패키지/엔티티를 Bar→Cafe, BarOwner→CafeOwner로 리네이밍하고, 신규 Organizer 엔티티를 추가한다. JWT에 ORGANIZER/CAFE_OWNER 유형을 추가하고, 이벤트 소유권을 Organizer로 이전한다. Commission은 이벤트당 2건(주관자용 + 카페용)으로 이원화한다.

**Tech Stack:** Kotlin 1.9, Spring Boot 3.2, Spring Data JPA, Spring Security, MySQL 8.0, Redis 7, jjwt 0.12, MockK

**Spec:** `docs/superpowers/specs/2026-03-23-frontend-design.md` (Section 1: 백엔드 변경 사항)

**Scope:** 이 계획은 역할 분리(Bar→Cafe, BarOwner→CafeOwner+Organizer)만 다룬다. Partnership과 MarketplacePost는 **Plan 2 (백엔드 마켓플레이스)** 에서 구현한다. 따라서 OrganizerService.createEvent에서 Partnership 존재 여부 검증은 Plan 2에서 추가한다.

**Migration 전략:** 기존 bar/barowner 패키지를 즉시 삭제하지 않고, 새 cafe/cafeowner/organizer 패키지를 먼저 생성한다. 모든 참조를 새 패키지로 이전한 뒤 빌드가 성공하면 마지막에 구 패키지를 삭제한다. 이를 통해 중간 커밋에서도 빌드가 깨지지 않는다.

---

## File Structure

### 삭제 대상 (기존 bar/barowner 패키지)
```
backend/src/main/kotlin/com/blinddate/bar/        ← 전체 삭제
backend/src/main/kotlin/com/blinddate/barowner/    ← 전체 삭제
backend/src/test/kotlin/com/blinddate/barowner/    ← 전체 삭제
backend/src/test/kotlin/com/blinddate/auth/service/BarOwnerAuthServiceTest.kt ← 삭제
```

### 신규 생성
```
backend/src/main/kotlin/com/blinddate/cafe/
├── entity/Cafe.kt
├── repository/CafeRepository.kt
├── controller/CafeController.kt
├── dto/CafeDtos.kt
└── service/CafeService.kt

backend/src/main/kotlin/com/blinddate/cafeowner/
├── entity/CafeOwner.kt
├── repository/CafeOwnerRepository.kt
├── controller/
│   ├── CafeOwnerAuthController.kt
│   ├── CafeOwnerCafeController.kt
│   ├── CafeOwnerPartnershipController.kt
│   ├── CafeOwnerEventController.kt
│   └── CafeOwnerCommissionController.kt
├── dto/CafeOwnerDtos.kt
└── service/CafeOwnerService.kt

backend/src/main/kotlin/com/blinddate/organizer/
├── entity/Organizer.kt
├── repository/OrganizerRepository.kt
├── controller/
│   ├── OrganizerAuthController.kt
│   ├── OrganizerEventController.kt
│   ├── OrganizerApplicationController.kt
│   ├── OrganizerDashboardController.kt
│   └── OrganizerCommissionController.kt
├── dto/OrganizerDtos.kt
└── service/OrganizerService.kt

backend/src/test/kotlin/com/blinddate/auth/service/CafeOwnerAuthServiceTest.kt
backend/src/test/kotlin/com/blinddate/auth/service/OrganizerAuthServiceTest.kt
backend/src/test/kotlin/com/blinddate/cafeowner/service/CafeOwnerServiceTest.kt
backend/src/test/kotlin/com/blinddate/organizer/service/OrganizerServiceTest.kt
```

### 수정 대상
```
backend/src/main/kotlin/com/blinddate/auth/jwt/UserType.kt
backend/src/main/kotlin/com/blinddate/auth/jwt/UserPrincipal.kt
backend/src/main/kotlin/com/blinddate/auth/jwt/JwtTokenProvider.kt
backend/src/main/kotlin/com/blinddate/auth/jwt/JwtAuthenticationFilter.kt
backend/src/main/kotlin/com/blinddate/auth/service/BarOwnerAuthService.kt → CafeOwnerAuthService.kt
backend/src/main/kotlin/com/blinddate/common/config/SecurityConfig.kt
backend/src/main/kotlin/com/blinddate/event/entity/Event.kt
backend/src/main/kotlin/com/blinddate/event/dto/EventDtos.kt
backend/src/main/kotlin/com/blinddate/event/repository/EventRepository.kt
backend/src/main/kotlin/com/blinddate/event/service/EventService.kt
backend/src/main/kotlin/com/blinddate/application/entity/Application.kt
backend/src/main/kotlin/com/blinddate/application/service/ApplicationService.kt
backend/src/main/kotlin/com/blinddate/action/entity/ActionToken.kt
backend/src/main/kotlin/com/blinddate/action/service/ActionTokenService.kt
backend/src/main/kotlin/com/blinddate/commission/entity/Commission.kt
backend/src/main/kotlin/com/blinddate/commission/service/CommissionService.kt
backend/src/main/kotlin/com/blinddate/commission/dto/CommissionDtos.kt
backend/src/main/kotlin/com/blinddate/commission/repository/CommissionRepository.kt
backend/src/main/kotlin/com/blinddate/notification/entity/RecipientType.kt
backend/src/main/kotlin/com/blinddate/notification/entity/NotificationType.kt
backend/src/main/kotlin/com/blinddate/notification/service/NotificationService.kt
backend/src/main/kotlin/com/blinddate/notification/controller/NotificationController.kt
backend/src/main/kotlin/com/blinddate/admin/controller/AdminBarController.kt → AdminCafeController.kt
backend/src/main/kotlin/com/blinddate/admin/dto/AdminDtos.kt
backend/src/main/kotlin/com/blinddate/admin/service/AdminService.kt
backend/src/main/kotlin/com/blinddate/admin/controller/AdminDashboardController.kt
backend/src/main/kotlin/com/blinddate/scheduler/EventStatusScheduler.kt
backend/src/main/kotlin/com/blinddate/scheduler/MatchingScheduler.kt
backend/src/main/kotlin/com/blinddate/scheduler/ReminderScheduler.kt

backend/src/test/kotlin/com/blinddate/auth/jwt/JwtTokenProviderTest.kt
backend/src/test/kotlin/com/blinddate/application/service/ApplicationServiceTest.kt
backend/src/test/kotlin/com/blinddate/event/service/EventServiceTest.kt
backend/src/test/kotlin/com/blinddate/commission/service/CommissionServiceTest.kt
backend/src/test/kotlin/com/blinddate/notification/service/NotificationServiceTest.kt
backend/src/test/kotlin/com/blinddate/action/service/ActionTokenServiceTest.kt
backend/src/test/kotlin/com/blinddate/admin/service/AdminServiceTest.kt
backend/src/test/kotlin/com/blinddate/matching/service/MatchingServiceTest.kt
```

---

## Task 1: Cafe 엔티티 + Repository (신규 생성, 기존 bar 유지)

새 cafe/ 패키지를 생성한다. 기존 bar/ 패키지는 아직 삭제하지 않는다 (모든 참조 이전 후 마지막에 삭제).

**Files:**
- Create: `cafe/entity/Cafe.kt`, `cafe/repository/CafeRepository.kt`

- [x] **Step 1: Cafe 엔티티 생성**

```kotlin
// cafe/entity/Cafe.kt
package com.blinddate.cafe.entity

import com.blinddate.common.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "cafe")
class Cafe(
    @Column(nullable = false) var name: String,
    @Column(nullable = false) var address: String,
    @Column(columnDefinition = "TEXT") var description: String = "",
    @Column var logoUrl: String = "",
    @Column var coverImageUrl: String = "",
    @Column(unique = true, nullable = false) val slug: String,
    var latitude: Double? = null,
    var longitude: Double? = null,
    @Column(nullable = false) var commissionRate: Int = 10,
    @Column(nullable = false) var isActive: Boolean = true
) : BaseEntity()
```

- [x] **Step 3: CafeRepository 생성**

```kotlin
// cafe/repository/CafeRepository.kt
package com.blinddate.cafe.repository

import com.blinddate.cafe.entity.Cafe
import org.springframework.data.jpa.repository.JpaRepository

interface CafeRepository : JpaRepository<Cafe, Long> {
    fun findBySlug(slug: String): Cafe?
    fun findByIsActiveTrue(): List<Cafe>
}
```

- [x] **Step 4: 빌드 확인 (성공 예상 — 기존 bar/ 패키지 유지 중)**

Run: `cd backend && ./gradlew compileKotlin 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL (기존 코드와 새 코드가 공존)

- [x] **Step 5: 커밋**

```bash
git add -A && git commit -m "feat: add Cafe entity with lat/lng fields (parallel to existing Bar)"
```

---

## Task 2: CafeOwner 엔티티 + Repository (신규 생성, 기존 barowner 유지)

새 cafeowner/ 패키지를 생성한다. 기존 barowner/ 패키지는 아직 삭제하지 않는다.

**Files:**
- Create: `cafeowner/entity/CafeOwner.kt`, `cafeowner/repository/CafeOwnerRepository.kt`

- [x] **Step 1: CafeOwner 엔티티 생성**

```kotlin
// cafeowner/entity/CafeOwner.kt
package com.blinddate.cafeowner.entity

import com.blinddate.cafe.entity.Cafe
import com.blinddate.common.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "cafe_owner")
class CafeOwner(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id", nullable = false)
    val cafe: Cafe,

    @Column(nullable = false) var name: String,
    @Column(nullable = false) var phoneNumber: String,
    @Column(unique = true, nullable = false) val email: String,
    @Column var kakaoId: String = "",
    @Column(nullable = false) var password: String
) : BaseEntity()
```

- [x] **Step 3: CafeOwnerRepository 생성**

```kotlin
// cafeowner/repository/CafeOwnerRepository.kt
package com.blinddate.cafeowner.repository

import com.blinddate.cafeowner.entity.CafeOwner
import org.springframework.data.jpa.repository.JpaRepository

interface CafeOwnerRepository : JpaRepository<CafeOwner, Long> {
    fun findByEmail(email: String): CafeOwner?
    fun findByCafeId(cafeId: Long): List<CafeOwner>
}
```

- [x] **Step 4: 커밋**

```bash
git add -A && git commit -m "refactor: rename BarOwner to CafeOwner entity"
```

---

## Task 3: Organizer 엔티티 + Repository

신규 Organizer 엔티티를 생성한다.

**Files:**
- Create: `organizer/entity/Organizer.kt`, `organizer/repository/OrganizerRepository.kt`

- [x] **Step 1: Organizer 엔티티 생성**

```kotlin
// organizer/entity/Organizer.kt
package com.blinddate.organizer.entity

import com.blinddate.common.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "organizer")
class Organizer(
    @Column(nullable = false) var name: String,
    @Column(nullable = false) var phoneNumber: String,
    @Column(unique = true, nullable = false) val email: String,
    @Column var kakaoId: String = "",
    @Column(nullable = false) var password: String,
    @Column(columnDefinition = "TEXT") var description: String = "",
    @Column(nullable = false) var commissionRate: Int = 15
) : BaseEntity()
```

- [x] **Step 2: OrganizerRepository 생성**

```kotlin
// organizer/repository/OrganizerRepository.kt
package com.blinddate.organizer.repository

import com.blinddate.organizer.entity.Organizer
import org.springframework.data.jpa.repository.JpaRepository

interface OrganizerRepository : JpaRepository<Organizer, Long> {
    fun findByEmail(email: String): Organizer?
}
```

- [x] **Step 3: 커밋**

```bash
git add -A && git commit -m "feat: add Organizer entity and repository"
```

---

## Task 4: UserType + JWT + Security 업데이트

인증 시스템에 ORGANIZER, CAFE_OWNER 유형을 추가한다.

**Files:**
- Modify: `auth/jwt/UserType.kt`, `auth/jwt/UserPrincipal.kt`, `auth/jwt/JwtTokenProvider.kt`, `auth/jwt/JwtAuthenticationFilter.kt`, `common/config/SecurityConfig.kt`

- [x] **Step 1: UserType enum 업데이트**

```kotlin
// auth/jwt/UserType.kt
package com.blinddate.auth.jwt
enum class UserType { PARTICIPANT, ORGANIZER, CAFE_OWNER, PLATFORM_ADMIN }
```

- [x] **Step 2: UserPrincipal 업데이트 (barId → cafeId)**

```kotlin
// auth/jwt/UserPrincipal.kt
package com.blinddate.auth.jwt
data class UserPrincipal(
    val id: Long,
    val userType: UserType,
    val cafeId: Long? = null
)
```

- [x] **Step 3: JwtTokenProvider 업데이트 (barId → cafeId)**

`JwtTokenProvider.kt`에서:
- `createAccessToken` 파라미터: `barId` → `cafeId`
- `buildToken` 파라미터: `barId` → `cafeId`
- `getUserPrincipal`에서: `claims["barId"]` → `claims["cafeId"]`, `barId =` → `cafeId =`
- `buildToken`에서: `builder.claim("barId", it)` → `builder.claim("cafeId", it)`

```kotlin
// 변경 부분만:
fun createAccessToken(userId: Long, userType: UserType, cafeId: Long? = null): String =
    buildToken(userId, userType, cafeId, accessTokenExpiry, "access")

fun getUserPrincipal(token: String): UserPrincipal {
    val claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
    return UserPrincipal(
        id = claims.subject.toLong(),
        userType = UserType.valueOf(claims["userType"] as String),
        cafeId = (claims["cafeId"] as? Number)?.toLong()
    )
}

private fun buildToken(userId: Long, userType: UserType, cafeId: Long?, expiry: Long, tokenType: String): String {
    val now = Date()
    val builder = Jwts.builder()
        .subject(userId.toString())
        .claim("userType", userType.name)
        .claim("tokenType", tokenType)
        .issuedAt(now)
        .expiration(Date(now.time + expiry))
        .signWith(key)
    cafeId?.let { builder.claim("cafeId", it) }
    return builder.compact()
}
```

- [x] **Step 4: JwtAuthenticationFilter 업데이트**

기존 `BAR_OWNER` → `CAFE_OWNER` 역할 매핑 변경. `ORGANIZER` 역할 추가.

```kotlin
// JwtAuthenticationFilter에서 authorities 매핑 부분:
val authorities = when (principal.userType) {
    UserType.PARTICIPANT -> listOf(SimpleGrantedAuthority("ROLE_PARTICIPANT"))
    UserType.ORGANIZER -> listOf(SimpleGrantedAuthority("ROLE_ORGANIZER"))
    UserType.CAFE_OWNER -> listOf(SimpleGrantedAuthority("ROLE_CAFE_OWNER"))
    UserType.PLATFORM_ADMIN -> listOf(SimpleGrantedAuthority("ROLE_PLATFORM_ADMIN"))
}
```

- [x] **Step 5: SecurityConfig 업데이트**

```kotlin
// SecurityConfig.kt의 authorizeHttpRequests 부분:
.authorizeHttpRequests {
    it
        .requestMatchers("/api/auth/**").permitAll()
        .requestMatchers("/api/organizer/auth/**").permitAll()
        .requestMatchers("/api/cafe-owner/auth/**").permitAll()
        .requestMatchers("/api/admin/auth/**").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/cafes/**").permitAll()
        .requestMatchers("/api/actions/**").permitAll()
        .requestMatchers("/api/marketplace/**").hasAnyRole("ORGANIZER", "CAFE_OWNER")
        .requestMatchers("/api/organizer/**").hasRole("ORGANIZER")
        .requestMatchers("/api/cafe-owner/**").hasRole("CAFE_OWNER")
        .requestMatchers("/api/admin/**").hasRole("PLATFORM_ADMIN")
        .anyRequest().authenticated()
}
```

- [x] **Step 6: 커밋**

```bash
git add -A && git commit -m "refactor: update auth system for ORGANIZER and CAFE_OWNER roles"
```

---

## Task 5: Event + Application 엔티티 업데이트

Event에 organizer FK 추가, Application의 reviewedBy를 Organizer로 변경.

**Files:**
- Modify: `event/entity/Event.kt`, `event/repository/EventRepository.kt`, `application/entity/Application.kt`

- [x] **Step 1: Event 엔티티 수정 (bar → cafe, organizer 추가)**

```kotlin
// event/entity/Event.kt — import 및 필드 변경
import com.blinddate.cafe.entity.Cafe
import com.blinddate.organizer.entity.Organizer

@Entity
@Table(name = "event")
class Event(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id", nullable = false)
    val cafe: Cafe,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    val organizer: Organizer,

    // ... 나머지 필드 동일
)
```

- [x] **Step 2: EventRepository 수정 (Bar → Cafe)**

```kotlin
// event/repository/EventRepository.kt
fun findByCafeSlugAndDeletedAtIsNull(slug: String): List<Event>
fun findByCafeIdAndDeletedAtIsNull(cafeId: Long): List<Event>
fun findByOrganizerIdAndDeletedAtIsNull(organizerId: Long): List<Event>
```

- [x] **Step 3: Application 엔티티 수정 (reviewedBy: BarOwner → Organizer)**

```kotlin
// application/entity/Application.kt — import 변경
import com.blinddate.organizer.entity.Organizer

// reviewedBy 필드 변경:
@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "reviewed_by")
var reviewedBy: Organizer? = null,
```

- [x] **Step 4: 커밋**

```bash
git add -A && git commit -m "refactor: update Event and Application entities for Organizer ownership"
```

---

## Task 6: ActionToken + Commission + Notification 엔티티 업데이트

ActionToken을 범용화하고, Commission을 이원화하고, Notification의 RecipientType을 확장한다.

**Files:**
- Modify: `action/entity/ActionToken.kt`, `commission/entity/Commission.kt`, `notification/entity/RecipientType.kt`, `notification/entity/NotificationType.kt`
- Create: `commission/entity/CommissionTargetType.kt`

- [x] **Step 1: ActionToken 수정 (barOwnerId → actorType + actorId)**

```kotlin
// action/entity/ActionToken.kt
package com.blinddate.action.entity

import com.blinddate.auth.jwt.UserType
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
    @Enumerated(EnumType.STRING) @Column(nullable = false) val actorType: UserType,
    @Column(nullable = false) val actorId: Long,
    @Column(nullable = false) var used: Boolean = false,
    @Column(nullable = false) val expiresAt: LocalDateTime = LocalDateTime.now().plusHours(24)
) : BaseEntity() {
    fun isExpired() = LocalDateTime.now().isAfter(expiresAt)
    fun isValid() = !used && !isExpired()
}
```

- [x] **Step 2: CommissionTargetType enum 생성**

```kotlin
// commission/entity/CommissionTargetType.kt
package com.blinddate.commission.entity
enum class CommissionTargetType { ORGANIZER, CAFE_OWNER }
```

- [x] **Step 3: Commission 엔티티 수정 (이원화)**

```kotlin
// commission/entity/Commission.kt
package com.blinddate.commission.entity

import com.blinddate.cafe.entity.Cafe
import com.blinddate.common.entity.BaseEntity
import com.blinddate.event.entity.Event
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "commission")
class Commission(
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "cafe_id", nullable = false) val cafe: Cafe,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "event_id", nullable = false) val event: Event,
    @Enumerated(EnumType.STRING) @Column(nullable = false) val targetType: CommissionTargetType,
    @Column(nullable = false) val targetId: Long,
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

- [x] **Step 4: RecipientType 확장**

```kotlin
// notification/entity/RecipientType.kt
package com.blinddate.notification.entity
enum class RecipientType { PARTICIPANT, ORGANIZER, CAFE_OWNER }
```

- [x] **Step 5: NotificationType 확장**

```kotlin
// notification/entity/NotificationType.kt
package com.blinddate.notification.entity
enum class NotificationType {
    NEW_APPLICATION, APPROVED, REJECTED, MATCH_RESULT,
    EVENT_REMINDER, CHOICE_REMINDER, EVENT_COMPLETED, COMMISSION_INVOICE,
    PARTNERSHIP_REQUESTED, PARTNERSHIP_ACCEPTED, PARTNERSHIP_REJECTED
}
```

- [x] **Step 6: 커밋**

```bash
git add -A && git commit -m "refactor: update ActionToken, Commission, Notification for role split"
```

---

## Task 7: Auth 서비스 업데이트 (CafeOwner + Organizer 인증)

기존 BarOwnerAuthService를 CafeOwnerAuthService로 교체하고, OrganizerAuthService를 추가한다.

**Files:**
- Create: `auth/service/CafeOwnerAuthService.kt`, `auth/service/OrganizerAuthService.kt`
- Modify: `auth/dto/AuthDtos.kt`, `auth/controller/AuthController.kt`

- [x] **Step 1: CafeOwnerAuthService 테스트 작성**

```kotlin
// test/.../auth/service/CafeOwnerAuthServiceTest.kt
package com.blinddate.auth.service

import com.blinddate.auth.jwt.JwtTokenProvider
import com.blinddate.auth.jwt.UserType
import com.blinddate.cafeowner.entity.CafeOwner
import com.blinddate.cafeowner.repository.CafeOwnerRepository
import com.blinddate.cafe.entity.Cafe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder

class CafeOwnerAuthServiceTest {
    private val cafeOwnerRepository = mockk<CafeOwnerRepository>()
    private val jwtTokenProvider = mockk<JwtTokenProvider>()
    private val passwordEncoder = mockk<PasswordEncoder>()
    private val service = CafeOwnerAuthService(cafeOwnerRepository, jwtTokenProvider, passwordEncoder)

    @Test
    fun `login succeeds with valid credentials`() {
        val cafe = mockk<Cafe> { every { id } returns 1L }
        val owner = mockk<CafeOwner> {
            every { id } returns 10L
            every { email } returns "owner@test.com"
            every { password } returns "encoded"
            every { this@mockk.cafe } returns cafe
        }
        every { cafeOwnerRepository.findByEmail("owner@test.com") } returns owner
        every { passwordEncoder.matches("pass123", "encoded") } returns true
        every { jwtTokenProvider.createAccessToken(10L, UserType.CAFE_OWNER, 1L) } returns "access"
        every { jwtTokenProvider.createRefreshToken(10L, UserType.CAFE_OWNER) } returns "refresh"

        val result = service.login("owner@test.com", "pass123")
        assertEquals("access", result.accessToken)
    }

    @Test
    fun `login fails with wrong password`() {
        val owner = mockk<CafeOwner> {
            every { password } returns "encoded"
        }
        every { cafeOwnerRepository.findByEmail("owner@test.com") } returns owner
        every { passwordEncoder.matches("wrong", "encoded") } returns false

        assertThrows(com.blinddate.common.exception.BadRequestException::class.java) {
            service.login("owner@test.com", "wrong")
        }
    }
}
```

- [x] **Step 2: 테스트 실패 확인**

Run: `cd backend && ./gradlew test --tests "com.blinddate.auth.service.CafeOwnerAuthServiceTest" 2>&1 | tail -5`
Expected: FAIL (CafeOwnerAuthService 미존재)

- [x] **Step 3: CafeOwnerAuthService 구현**

```kotlin
// auth/service/CafeOwnerAuthService.kt
package com.blinddate.auth.service

import com.blinddate.auth.dto.LoginRequest
import com.blinddate.auth.dto.TokenResponse
import com.blinddate.auth.jwt.JwtTokenProvider
import com.blinddate.auth.jwt.UserType
import com.blinddate.cafeowner.repository.CafeOwnerRepository
import com.blinddate.common.exception.BadRequestException
import com.blinddate.common.exception.NotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class CafeOwnerAuthService(
    private val cafeOwnerRepository: CafeOwnerRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder
) {
    fun login(email: String, password: String): TokenResponse {
        val owner = cafeOwnerRepository.findByEmail(email) ?: throw NotFoundException("계정을 찾을 수 없습니다")
        if (!passwordEncoder.matches(password, owner.password)) throw BadRequestException("비밀번호가 일치하지 않습니다")
        return TokenResponse(
            accessToken = jwtTokenProvider.createAccessToken(owner.id, UserType.CAFE_OWNER, owner.cafe.id),
            refreshToken = jwtTokenProvider.createRefreshToken(owner.id, UserType.CAFE_OWNER)
        )
    }
}
```

- [x] **Step 4: OrganizerAuthService 테스트 작성**

```kotlin
// test/.../auth/service/OrganizerAuthServiceTest.kt
package com.blinddate.auth.service

import com.blinddate.auth.jwt.JwtTokenProvider
import com.blinddate.auth.jwt.UserType
import com.blinddate.organizer.entity.Organizer
import com.blinddate.organizer.repository.OrganizerRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder

class OrganizerAuthServiceTest {
    private val organizerRepository = mockk<OrganizerRepository>()
    private val jwtTokenProvider = mockk<JwtTokenProvider>()
    private val passwordEncoder = mockk<PasswordEncoder>()
    private val service = OrganizerAuthService(organizerRepository, jwtTokenProvider, passwordEncoder)

    @Test
    fun `login succeeds with valid credentials`() {
        val organizer = mockk<Organizer> {
            every { id } returns 20L
            every { email } returns "org@test.com"
            every { password } returns "encoded"
        }
        every { organizerRepository.findByEmail("org@test.com") } returns organizer
        every { passwordEncoder.matches("pass123", "encoded") } returns true
        every { jwtTokenProvider.createAccessToken(20L, UserType.ORGANIZER, null) } returns "access"
        every { jwtTokenProvider.createRefreshToken(20L, UserType.ORGANIZER) } returns "refresh"

        val result = service.login("org@test.com", "pass123")
        assertEquals("access", result.accessToken)
    }
}
```

- [x] **Step 5: OrganizerAuthService 구현**

```kotlin
// auth/service/OrganizerAuthService.kt
package com.blinddate.auth.service

import com.blinddate.auth.dto.TokenResponse
import com.blinddate.auth.jwt.JwtTokenProvider
import com.blinddate.auth.jwt.UserType
import com.blinddate.common.exception.BadRequestException
import com.blinddate.common.exception.NotFoundException
import com.blinddate.organizer.repository.OrganizerRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class OrganizerAuthService(
    private val organizerRepository: OrganizerRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder
) {
    fun login(email: String, password: String): TokenResponse {
        val organizer = organizerRepository.findByEmail(email) ?: throw NotFoundException("계정을 찾을 수 없습니다")
        if (!passwordEncoder.matches(password, organizer.password)) throw BadRequestException("비밀번호가 일치하지 않습니다")
        return TokenResponse(
            accessToken = jwtTokenProvider.createAccessToken(organizer.id, UserType.ORGANIZER),
            refreshToken = jwtTokenProvider.createRefreshToken(organizer.id, UserType.ORGANIZER)
        )
    }
}
```

- [x] **Step 6: AuthDtos 업데이트 (TokenResponse에 user 정보 포함되도록)**

기존 `AuthDtos.kt`를 확인하고, `LoginRequest` + `TokenResponse`가 있으면 유지. 없으면 추가.

- [x] **Step 7: 테스트 통과 확인**

Run: `cd backend && ./gradlew test --tests "com.blinddate.auth.service.*AuthServiceTest" 2>&1 | tail -10`
Expected: PASS

- [x] **Step 8: 커밋**

```bash
git add -A && git commit -m "feat: add CafeOwnerAuthService and OrganizerAuthService"
```

---

## Task 8: Cafe 공개 API + CafeOwner 서비스/컨트롤러

Cafe 공개 조회 API와 CafeOwner 관리 API를 구현한다.

**Files:**
- Create: `cafe/dto/CafeDtos.kt`, `cafe/service/CafeService.kt`, `cafe/controller/CafeController.kt`
- Create: `cafeowner/dto/CafeOwnerDtos.kt`, `cafeowner/service/CafeOwnerService.kt`
- Create: `cafeowner/controller/CafeOwnerAuthController.kt`, `cafeowner/controller/CafeOwnerCafeController.kt`

- [x] **Step 1: CafeDtos 생성**

```kotlin
// cafe/dto/CafeDtos.kt
package com.blinddate.cafe.dto

import com.blinddate.cafe.entity.Cafe

data class CafeResponse(
    val id: Long, val name: String, val address: String, val description: String,
    val logoUrl: String, val coverImageUrl: String, val slug: String,
    val latitude: Double?, val longitude: Double?, val isActive: Boolean
) {
    companion object {
        fun from(cafe: Cafe) = CafeResponse(
            id = cafe.id, name = cafe.name, address = cafe.address, description = cafe.description,
            logoUrl = cafe.logoUrl, coverImageUrl = cafe.coverImageUrl, slug = cafe.slug,
            latitude = cafe.latitude, longitude = cafe.longitude, isActive = cafe.isActive
        )
    }
}
```

- [x] **Step 2: CafeService 구현**

```kotlin
// cafe/service/CafeService.kt
package com.blinddate.cafe.service

import com.blinddate.cafe.dto.CafeResponse
import com.blinddate.cafe.repository.CafeRepository
import com.blinddate.common.exception.NotFoundException
import org.springframework.stereotype.Service

@Service
class CafeService(private val cafeRepository: CafeRepository) {
    fun getBySlug(slug: String): CafeResponse {
        val cafe = cafeRepository.findBySlug(slug) ?: throw NotFoundException("카페를 찾을 수 없습니다")
        return CafeResponse.from(cafe)
    }
}
```

- [x] **Step 3: CafeController 구현 (공개 API)**

```kotlin
// cafe/controller/CafeController.kt
package com.blinddate.cafe.controller

import com.blinddate.cafe.service.CafeService
import com.blinddate.event.service.EventService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/cafes")
class CafeController(
    private val cafeService: CafeService,
    private val eventService: EventService
) {
    @GetMapping("/{slug}")
    fun getCafe(@PathVariable slug: String) = cafeService.getBySlug(slug)

    @GetMapping("/{slug}/events")
    fun getEvents(@PathVariable slug: String) = eventService.getByCafeSlug(slug)

    @GetMapping("/{slug}/events/{eventId}")
    fun getEvent(@PathVariable slug: String, @PathVariable eventId: Long) = eventService.getById(eventId)
}
```

- [x] **Step 4: CafeOwnerDtos 생성**

```kotlin
// cafeowner/dto/CafeOwnerDtos.kt
package com.blinddate.cafeowner.dto

data class UpdateCafeRequest(
    val name: String?, val address: String?, val description: String?,
    val logoUrl: String?, val coverImageUrl: String?,
    val latitude: Double?, val longitude: Double?
)
```

- [x] **Step 5: CafeOwnerService 테스트 작성**

```kotlin
// test/.../cafeowner/service/CafeOwnerServiceTest.kt
package com.blinddate.cafeowner.service

import com.blinddate.cafe.dto.CafeResponse
import com.blinddate.cafe.entity.Cafe
import com.blinddate.cafe.repository.CafeRepository
import com.blinddate.common.exception.NotFoundException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.Optional

class CafeOwnerServiceTest {
    private val cafeRepository = mockk<CafeRepository>()
    private val service = CafeOwnerService(cafeRepository)

    @Test
    fun `getMyCafe returns cafe info`() {
        val cafe = Cafe(name = "Test Cafe", address = "Seoul", slug = "test")
        every { cafeRepository.findById(1L) } returns Optional.of(cafe)
        val result = service.getMyCafe(1L)
        assertEquals("Test Cafe", result.name)
    }

    @Test
    fun `getMyCafe throws when not found`() {
        every { cafeRepository.findById(999L) } returns Optional.empty()
        assertThrows(NotFoundException::class.java) { service.getMyCafe(999L) }
    }

    @Test
    fun `updateMyCafe updates fields`() {
        val cafe = Cafe(name = "Old", address = "Old Addr", slug = "test")
        every { cafeRepository.findById(1L) } returns Optional.of(cafe)
        every { cafeRepository.save(any()) } returns cafe

        service.updateMyCafe(1L, com.blinddate.cafeowner.dto.UpdateCafeRequest(
            name = "New", address = null, description = null,
            logoUrl = null, coverImageUrl = null, latitude = 37.5, longitude = 127.0
        ))
        assertEquals("New", cafe.name)
        assertEquals(37.5, cafe.latitude)
    }
}
```

- [x] **Step 6: CafeOwnerService 구현**

```kotlin
// cafeowner/service/CafeOwnerService.kt
package com.blinddate.cafeowner.service

import com.blinddate.cafe.dto.CafeResponse
import com.blinddate.cafe.repository.CafeRepository
import com.blinddate.cafeowner.dto.UpdateCafeRequest
import com.blinddate.common.exception.NotFoundException
import org.springframework.stereotype.Service

@Service
class CafeOwnerService(private val cafeRepository: CafeRepository) {
    fun getMyCafe(cafeId: Long): CafeResponse {
        val cafe = cafeRepository.findById(cafeId).orElseThrow { NotFoundException("카페를 찾을 수 없습니다") }
        return CafeResponse.from(cafe)
    }

    fun updateMyCafe(cafeId: Long, request: UpdateCafeRequest): CafeResponse {
        val cafe = cafeRepository.findById(cafeId).orElseThrow { NotFoundException("카페를 찾을 수 없습니다") }
        request.name?.let { cafe.name = it }
        request.address?.let { cafe.address = it }
        request.description?.let { cafe.description = it }
        request.logoUrl?.let { cafe.logoUrl = it }
        request.coverImageUrl?.let { cafe.coverImageUrl = it }
        request.latitude?.let { cafe.latitude = it }
        request.longitude?.let { cafe.longitude = it }
        cafeRepository.save(cafe)
        return CafeResponse.from(cafe)
    }
}
```

- [x] **Step 7: CafeOwner 컨트롤러 구현**

```kotlin
// cafeowner/controller/CafeOwnerAuthController.kt
package com.blinddate.cafeowner.controller

import com.blinddate.auth.dto.LoginRequest
import com.blinddate.auth.service.CafeOwnerAuthService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/cafe-owner/auth")
class CafeOwnerAuthController(private val authService: CafeOwnerAuthService) {
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest) = authService.login(request.email, request.password)
}
```

```kotlin
// cafeowner/controller/CafeOwnerCafeController.kt
package com.blinddate.cafeowner.controller

import com.blinddate.auth.jwt.UserPrincipal
import com.blinddate.cafeowner.dto.UpdateCafeRequest
import com.blinddate.cafeowner.service.CafeOwnerService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/cafe-owner")
class CafeOwnerCafeController(private val cafeOwnerService: CafeOwnerService) {
    @GetMapping("/my-cafe")
    fun getMyCafe(@AuthenticationPrincipal principal: UserPrincipal) =
        cafeOwnerService.getMyCafe(principal.cafeId!!)

    @PutMapping("/my-cafe")
    fun updateMyCafe(@AuthenticationPrincipal principal: UserPrincipal, @RequestBody request: UpdateCafeRequest) =
        cafeOwnerService.updateMyCafe(principal.cafeId!!, request)
}
```

- [x] **Step 8: 테스트 통과 확인**

Run: `cd backend && ./gradlew test --tests "com.blinddate.cafeowner.service.*" 2>&1 | tail -5`
Expected: PASS

- [x] **Step 9: 커밋**

```bash
git add -A && git commit -m "feat: add Cafe public API and CafeOwner management controllers"
```

---

## Task 9: Organizer 이벤트/신청 서비스 + 컨트롤러

주관자의 이벤트 CRUD와 신청자 관리를 구현한다. 기존 BarOwner가 하던 역할을 Organizer가 담당한다.

**Files:**
- Create: `organizer/dto/OrganizerDtos.kt`, `organizer/service/OrganizerService.kt`
- Create: `organizer/controller/OrganizerAuthController.kt`, `organizer/controller/OrganizerEventController.kt`, `organizer/controller/OrganizerApplicationController.kt`
- Modify: `event/service/EventService.kt`, `event/dto/EventDtos.kt`, `application/service/ApplicationService.kt`

- [x] **Step 1: OrganizerDtos 생성**

```kotlin
// organizer/dto/OrganizerDtos.kt
package com.blinddate.organizer.dto

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class CreateEventRequest(
    val cafeId: Long,
    val title: String,
    val date: LocalDate,
    val time: LocalTime,
    val price: Int,
    val maleCapacity: Int,
    val femaleCapacity: Int,
    val description: String = "",
    val choiceDeadline: LocalDateTime? = null,
    val matchNotificationTime: LocalDateTime? = null,
    val minAge: Int? = null,
    val maxAge: Int? = null,
    val maxChoices: Int = 3
)

data class UpdateEventRequest(
    val title: String? = null,
    val date: LocalDate? = null,
    val time: LocalTime? = null,
    val price: Int? = null,
    val maleCapacity: Int? = null,
    val femaleCapacity: Int? = null,
    val description: String? = null,
    val choiceDeadline: LocalDateTime? = null,
    val minAge: Int? = null,
    val maxAge: Int? = null,
    val maxChoices: Int? = null
)
```

- [x] **Step 2: OrganizerService 테스트 작성 (이벤트 생성, 수정, 삭제)**

```kotlin
// test/.../organizer/service/OrganizerServiceTest.kt
package com.blinddate.organizer.service

import com.blinddate.cafe.entity.Cafe
import com.blinddate.cafe.repository.CafeRepository
import com.blinddate.common.exception.BadRequestException
import com.blinddate.event.entity.Event
import com.blinddate.event.entity.EventStatus
import com.blinddate.event.repository.EventRepository
import com.blinddate.organizer.dto.CreateEventRequest
import com.blinddate.organizer.entity.Organizer
import com.blinddate.organizer.repository.OrganizerRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import java.util.Optional

class OrganizerServiceTest {
    private val eventRepository = mockk<EventRepository>()
    private val cafeRepository = mockk<CafeRepository>()
    private val organizerRepository = mockk<OrganizerRepository>()
    private val service = OrganizerService(eventRepository, cafeRepository, organizerRepository)

    @Test
    fun `createEvent succeeds with valid cafe`() {
        val organizer = mockk<Organizer> { every { id } returns 1L }
        val cafe = mockk<Cafe> { every { id } returns 10L }
        every { organizerRepository.findById(1L) } returns Optional.of(organizer)
        every { cafeRepository.findById(10L) } returns Optional.of(cafe)
        every { eventRepository.save(any()) } answers { firstArg() }

        val request = CreateEventRequest(
            cafeId = 10L, title = "Friday", date = LocalDate.now().plusDays(7),
            time = LocalTime.of(20, 0), price = 30000, maleCapacity = 5, femaleCapacity = 5
        )
        val result = service.createEvent(1L, request)
        assertEquals("Friday", result.title)
    }

    @Test
    fun `deleteEvent only soft deletes own event`() {
        val organizer = mockk<Organizer> { every { id } returns 1L }
        val event = Event(
            cafe = mockk(), organizer = organizer, title = "Test",
            date = LocalDate.now(), time = LocalTime.of(20, 0),
            price = 30000, maleCapacity = 5, femaleCapacity = 5
        )
        every { eventRepository.findById(100L) } returns Optional.of(event)
        every { eventRepository.save(any()) } answers { firstArg() }

        service.deleteEvent(1L, 100L)
        assertNotNull(event.deletedAt)
    }

    @Test
    fun `deleteEvent fails for other organizer's event`() {
        val otherOrganizer = mockk<Organizer> { every { id } returns 2L }
        val event = Event(
            cafe = mockk(), organizer = otherOrganizer, title = "Test",
            date = LocalDate.now(), time = LocalTime.of(20, 0),
            price = 30000, maleCapacity = 5, femaleCapacity = 5
        )
        every { eventRepository.findById(100L) } returns Optional.of(event)

        assertThrows(BadRequestException::class.java) { service.deleteEvent(1L, 100L) }
    }
}
```

- [x] **Step 3: OrganizerService 구현**

```kotlin
// organizer/service/OrganizerService.kt
package com.blinddate.organizer.service

import com.blinddate.cafe.repository.CafeRepository
import com.blinddate.common.exception.BadRequestException
import com.blinddate.common.exception.NotFoundException
import com.blinddate.event.dto.EventResponse
import com.blinddate.event.entity.Event
import com.blinddate.event.entity.EventStatus
import com.blinddate.event.repository.EventRepository
import com.blinddate.organizer.dto.CreateEventRequest
import com.blinddate.organizer.dto.UpdateEventRequest
import com.blinddate.organizer.repository.OrganizerRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class OrganizerService(
    private val eventRepository: EventRepository,
    private val cafeRepository: CafeRepository,
    private val organizerRepository: OrganizerRepository
) {
    fun createEvent(organizerId: Long, request: CreateEventRequest): EventResponse {
        val organizer = organizerRepository.findById(organizerId).orElseThrow { NotFoundException("주관자를 찾을 수 없습니다") }
        val cafe = cafeRepository.findById(request.cafeId).orElseThrow { NotFoundException("카페를 찾을 수 없습니다") }
        val event = eventRepository.save(Event(
            cafe = cafe, organizer = organizer, title = request.title,
            date = request.date, time = request.time, price = request.price,
            maleCapacity = request.maleCapacity, femaleCapacity = request.femaleCapacity,
            description = request.description, choiceDeadline = request.choiceDeadline,
            matchNotificationTime = request.matchNotificationTime,
            minAge = request.minAge, maxAge = request.maxAge, maxChoices = request.maxChoices
        ))
        return EventResponse.from(event)
    }

    fun getMyEvents(organizerId: Long): List<EventResponse> =
        eventRepository.findByOrganizerIdAndDeletedAtIsNull(organizerId).map { EventResponse.from(it) }

    fun updateEvent(organizerId: Long, eventId: Long, request: UpdateEventRequest): EventResponse {
        val event = getOwnEvent(organizerId, eventId)
        request.title?.let { event.title = it }
        request.date?.let { event.date = it }
        request.time?.let { event.time = it }
        request.price?.let { event.price = it }
        request.maleCapacity?.let { event.maleCapacity = it }
        request.femaleCapacity?.let { event.femaleCapacity = it }
        request.description?.let { event.description = it }
        request.choiceDeadline?.let { event.choiceDeadline = it }
        request.minAge?.let { event.minAge = it }
        request.maxAge?.let { event.maxAge = it }
        request.maxChoices?.let { event.maxChoices = it }
        return EventResponse.from(eventRepository.save(event))
    }

    fun deleteEvent(organizerId: Long, eventId: Long) {
        val event = getOwnEvent(organizerId, eventId)
        event.deletedAt = LocalDateTime.now()
        eventRepository.save(event)
    }

    fun closeEvent(organizerId: Long, eventId: Long): EventResponse {
        val event = getOwnEvent(organizerId, eventId)
        if (event.status != EventStatus.OPEN) throw BadRequestException("OPEN 상태의 이벤트만 마감할 수 있습니다")
        event.status = EventStatus.CLOSED
        return EventResponse.from(eventRepository.save(event))
    }

    private fun getOwnEvent(organizerId: Long, eventId: Long): Event {
        val event = eventRepository.findById(eventId).orElseThrow { NotFoundException("이벤트를 찾을 수 없습니다") }
        if (event.organizer.id != organizerId) throw BadRequestException("본인의 이벤트만 관리할 수 있습니다")
        if (event.isDeleted()) throw NotFoundException("삭제된 이벤트입니다")
        return event
    }
}
```

- [x] **Step 4: Organizer 컨트롤러 구현**

```kotlin
// organizer/controller/OrganizerAuthController.kt
package com.blinddate.organizer.controller

import com.blinddate.auth.dto.LoginRequest
import com.blinddate.auth.service.OrganizerAuthService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/organizer/auth")
class OrganizerAuthController(private val authService: OrganizerAuthService) {
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest) = authService.login(request.email, request.password)
}
```

```kotlin
// organizer/controller/OrganizerEventController.kt
package com.blinddate.organizer.controller

import com.blinddate.auth.jwt.UserPrincipal
import com.blinddate.organizer.dto.CreateEventRequest
import com.blinddate.organizer.dto.UpdateEventRequest
import com.blinddate.organizer.service.OrganizerService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/organizer/events")
class OrganizerEventController(private val organizerService: OrganizerService) {
    @GetMapping
    fun getEvents(@AuthenticationPrincipal p: UserPrincipal) = organizerService.getMyEvents(p.id)

    @PostMapping
    fun createEvent(@AuthenticationPrincipal p: UserPrincipal, @RequestBody req: CreateEventRequest) =
        organizerService.createEvent(p.id, req)

    @PutMapping("/{id}")
    fun updateEvent(@AuthenticationPrincipal p: UserPrincipal, @PathVariable id: Long, @RequestBody req: UpdateEventRequest) =
        organizerService.updateEvent(p.id, id, req)

    @DeleteMapping("/{id}")
    fun deleteEvent(@AuthenticationPrincipal p: UserPrincipal, @PathVariable id: Long) =
        organizerService.deleteEvent(p.id, id)

    @PutMapping("/{id}/close")
    fun closeEvent(@AuthenticationPrincipal p: UserPrincipal, @PathVariable id: Long) =
        organizerService.closeEvent(p.id, id)
}
```

```kotlin
// organizer/controller/OrganizerApplicationController.kt
package com.blinddate.organizer.controller

import com.blinddate.application.service.ApplicationService
import com.blinddate.auth.jwt.UserPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/organizer")
class OrganizerApplicationController(private val applicationService: ApplicationService) {
    @GetMapping("/events/{eventId}/applications")
    fun getApplications(@AuthenticationPrincipal p: UserPrincipal, @PathVariable eventId: Long) =
        applicationService.getByEventForOrganizer(p.id, eventId)

    @PutMapping("/applications/{id}/approve")
    fun approve(@AuthenticationPrincipal p: UserPrincipal, @PathVariable id: Long) =
        applicationService.approve(p.id, id)

    @PutMapping("/applications/{id}/reject")
    fun reject(@AuthenticationPrincipal p: UserPrincipal, @PathVariable id: Long, @RequestBody body: Map<String, String>) =
        applicationService.reject(p.id, id, body["reason"])
}
```

- [x] **Step 5: EventService, EventDtos 업데이트 (Bar → Cafe 참조)**

기존 `EventService`와 `EventDtos`에서 `bar` → `cafe`, `barId` → `cafeId` 참조를 일괄 변경. `EventResponse`에 `organizerId` 필드 추가.

- [x] **Step 6: ApplicationService 업데이트 (BarOwner → Organizer 참조)**

기존 `ApplicationService`에서:
- `BarOwnerRepository` → `OrganizerRepository` 의존성 변경
- approve/reject 메서드에서 `barOwner` → `organizer` 참조
- `getByEventForBarOwner` → `getByEventForOrganizer` 메서드명 변경

- [x] **Step 7: 테스트 통과 확인**

Run: `cd backend && ./gradlew test --tests "com.blinddate.organizer.service.*" 2>&1 | tail -10`
Expected: PASS

- [x] **Step 8: 커밋**

```bash
git add -A && git commit -m "feat: add Organizer event CRUD and application management"
```

---

## Task 10: Commission 서비스 이원화 + Admin 업데이트

Commission을 이벤트당 2건 생성하도록 변경하고, Admin 컨트롤러를 Cafe/Organizer 구조에 맞게 수정한다.

**Files:**
- Modify: `commission/service/CommissionService.kt`, `commission/dto/CommissionDtos.kt`, `commission/repository/CommissionRepository.kt`
- Modify: `admin/controller/AdminBarController.kt` → 삭제, 새로 `admin/controller/AdminCafeController.kt` 생성
- Modify: `admin/dto/AdminDtos.kt`, `admin/service/AdminService.kt`
- Create: `admin/controller/AdminOrganizerController.kt`

- [x] **Step 1: CommissionRepository 업데이트**

```kotlin
// commission/repository/CommissionRepository.kt
interface CommissionRepository : JpaRepository<Commission, Long> {
    fun findByCafeId(cafeId: Long): List<Commission>
    fun findByTargetTypeAndTargetId(targetType: CommissionTargetType, targetId: Long): List<Commission>
    fun findByEventId(eventId: Long): List<Commission>
}
```

- [x] **Step 2: CommissionService 테스트 작성 (이원화: 이벤트당 2건)**

```kotlin
// test에서: createForEvent 호출 시 Commission 2건 (ORGANIZER용, CAFE_OWNER용) 생성 검증
// save가 2번 호출되는지, 각각의 targetType/commissionRate이 올바른지 확인
```

- [x] **Step 3: CommissionService 구현 (이원화)**

`createForEvent`에서:
1. Event 조회 → cafe.commissionRate, organizer.commissionRate 가져옴
2. APPROVED 참가자 수 계산
3. Commission 2건 생성: `CommissionTargetType.ORGANIZER` + `CommissionTargetType.CAFE_OWNER`

- [x] **Step 4: Admin 컨트롤러 변경**

- `AdminBarController.kt` 삭제 → `AdminCafeController.kt` 생성 (`/api/admin/cafes`, `/api/admin/cafe-owners`)
- `AdminOrganizerController.kt` 생성 (`/api/admin/organizers`)
- `AdminService`에 `createOrganizer`, `getOrganizers` 메서드 추가
- `AdminDtos`에 `CreateOrganizerRequest` 추가

- [x] **Step 5: AdminDashboard 업데이트**

기존 통계에 Organizer 관련 통계 추가.

- [x] **Step 6: 테스트 통과 확인**

Run: `cd backend && ./gradlew test --tests "com.blinddate.commission.service.*" --tests "com.blinddate.admin.service.*" 2>&1 | tail -10`

- [x] **Step 7: 커밋**

```bash
git add -A && git commit -m "feat: dualize Commission and update Admin for Cafe/Organizer structure"
```

---

## Task 11: Notification + ActionToken 서비스 업데이트

알림 시스템과 ActionToken 서비스를 새 역할 구조에 맞게 수정한다.

**Files:**
- Modify: `notification/service/NotificationService.kt`, `notification/controller/NotificationController.kt`
- Modify: `action/service/ActionTokenService.kt`, `action/dto/ActionDtos.kt`

- [x] **Step 1: NotificationController 업데이트**

기존: PARTICIPANT만 알림 조회 가능.
변경: ORGANIZER, CAFE_OWNER도 각자의 알림 조회 가능.

```kotlin
// 주관자 알림 엔드포인트 추가:
// GET /api/organizer/notifications
// PUT /api/organizer/notifications/{id}/read
// 카페 주인 알림 엔드포인트:
// GET /api/cafe-owner/notifications
// PUT /api/cafe-owner/notifications/{id}/read
```

각 역할의 컨트롤러에 알림 관련 메서드를 추가하거나, NotificationController를 범용화.

- [x] **Step 2: ActionTokenService 업데이트**

- `createToken` 메서드: `barOwnerId` 파라미터 → `actorType: UserType, actorId: Long`
- `executeAction`: ActionToken의 `actorType`에 따라 적절한 서비스 호출

- [x] **Step 3: ActionTokenService 테스트 업데이트**

기존 테스트에서 `barOwnerId` → `actorType + actorId`로 변경.

- [x] **Step 4: 테스트 통과 확인**

Run: `cd backend && ./gradlew test --tests "com.blinddate.action.service.*" --tests "com.blinddate.notification.service.*" 2>&1 | tail -10`

- [x] **Step 5: 커밋**

```bash
git add -A && git commit -m "refactor: update Notification and ActionToken for multi-role support"
```

---

## Task 12: 스케줄러 업데이트

3개 스케줄러의 Bar/BarOwner 참조를 Cafe/Organizer로 변경한다.

**Files:**
- Modify: `scheduler/EventStatusScheduler.kt`, `scheduler/MatchingScheduler.kt`, `scheduler/ReminderScheduler.kt`

- [x] **Step 1: EventStatusScheduler 수정**

Bar → Cafe 참조 변경. 로직은 동일.

- [x] **Step 2: MatchingScheduler 수정**

- Bar → Cafe 참조 변경
- 매칭 완료 알림: BarOwner → Organizer에게 발송
- RecipientType.BAR_OWNER → RecipientType.ORGANIZER

- [x] **Step 3: ReminderScheduler 수정**

- 참가자 리마인더: 기존 동일
- 카페 주인 리마인더 추가: RecipientType.CAFE_OWNER에게 이벤트 전날 알림

- [x] **Step 4: 커밋**

```bash
git add -A && git commit -m "refactor: update schedulers for Cafe/Organizer references"
```

---

## Task 13: 기존 테스트 전체 수정 + 빌드 확인

남은 모든 테스트를 새 구조에 맞게 수정하고, 전체 빌드/테스트를 통과시킨다.

**Files:**
- Modify: 모든 기존 테스트 파일의 Bar/BarOwner 참조를 Cafe/CafeOwner/Organizer로 변경

- [x] **Step 1: JwtTokenProviderTest 수정**

`BAR_OWNER` → `CAFE_OWNER`, `barId` → `cafeId` 참조 변경. ORGANIZER 토큰 테스트 추가.

- [x] **Step 2: EventServiceTest 수정**

Bar → Cafe, Event에 organizer 추가.

- [x] **Step 3: ApplicationServiceTest 수정**

BarOwner → Organizer 참조 변경.

- [x] **Step 4: MatchingServiceTest 수정**

Bar → Cafe 참조 변경.

- [x] **Step 5: CommissionServiceTest 수정**

이원화 로직 반영.

- [x] **Step 6: NotificationServiceTest 수정**

RecipientType.BAR_OWNER → ORGANIZER/CAFE_OWNER.

- [x] **Step 7: ActionTokenServiceTest 수정**

barOwnerId → actorType + actorId.

- [x] **Step 8: AdminServiceTest 수정**

Bar → Cafe, Organizer 관련 테스트 추가.

- [x] **Step 9: 전체 빌드 확인**

Run: `cd backend && ./gradlew build`
Expected: BUILD SUCCESSFUL

- [x] **Step 10: 전체 테스트 실행**

Run: `cd backend && ./gradlew test`
Expected: 모든 테스트 PASS

- [x] **Step 11: 커밋**

```bash
git add -A && git commit -m "test: update all tests for Cafe/Organizer restructure"
```

---

## Task 14: CafeOwner 추가 컨트롤러 (이벤트 조회, 수수료)

카페 주인이 자기 카페의 이벤트(읽기 전용)와 수수료를 조회할 수 있는 엔드포인트를 추가한다.

**Files:**
- Create: `cafeowner/controller/CafeOwnerEventController.kt`, `cafeowner/controller/CafeOwnerCommissionController.kt`
- Create: `organizer/controller/OrganizerCommissionController.kt`

- [x] **Step 1: CafeOwnerEventController 구현**

```kotlin
// cafeowner/controller/CafeOwnerEventController.kt
@RestController @RequestMapping("/api/cafe-owner/events")
class CafeOwnerEventController(private val eventService: EventService) {
    @GetMapping
    fun getEvents(@AuthenticationPrincipal p: UserPrincipal) = eventService.getByCafeId(p.cafeId!!)
}
```

- [x] **Step 2: CafeOwnerCommissionController 구현**

```kotlin
// cafeowner/controller/CafeOwnerCommissionController.kt
@RestController @RequestMapping("/api/cafe-owner/commissions")
class CafeOwnerCommissionController(private val commissionService: CommissionService) {
    @GetMapping
    fun getCommissions(@AuthenticationPrincipal p: UserPrincipal) =
        commissionService.getByTargetType(CommissionTargetType.CAFE_OWNER, p.id)
}
```

- [x] **Step 3: OrganizerCommissionController 구현**

```kotlin
// organizer/controller/OrganizerCommissionController.kt
@RestController @RequestMapping("/api/organizer/commissions")
class OrganizerCommissionController(private val commissionService: CommissionService) {
    @GetMapping
    fun getCommissions(@AuthenticationPrincipal p: UserPrincipal) =
        commissionService.getByTargetType(CommissionTargetType.ORGANIZER, p.id)
}
```

- [x] **Step 4: 빌드 확인**

Run: `cd backend && ./gradlew build`
Expected: BUILD SUCCESSFUL

- [x] **Step 5: 커밋**

```bash
git add -A && git commit -m "feat: add CafeOwner event/commission and Organizer commission controllers"
```

---

## Task 15: 누락 컨트롤러 추가 (Dashboard, Notification)

스펙에 정의된 대시보드와 알림 컨트롤러를 추가한다.

**Files:**
- Create: `organizer/controller/OrganizerDashboardController.kt`
- Create: `cafeowner/controller/CafeOwnerDashboardController.kt`
- Add notification endpoints to: `organizer/controller/OrganizerNotificationController.kt`, `cafeowner/controller/CafeOwnerNotificationController.kt`

- [x] **Step 1: OrganizerDashboardController 구현**

```kotlin
// organizer/controller/OrganizerDashboardController.kt
package com.blinddate.organizer.controller

import com.blinddate.auth.jwt.UserPrincipal
import com.blinddate.organizer.service.OrganizerService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/organizer/dashboard")
class OrganizerDashboardController(private val organizerService: OrganizerService) {
    @GetMapping
    fun getDashboard(@AuthenticationPrincipal p: UserPrincipal) = organizerService.getDashboard(p.id)
}
```

OrganizerService에 `getDashboard(organizerId)` 메서드 추가: 진행 중 이벤트 수, 총 참가자 수, 최근 이벤트 등.

- [x] **Step 2: CafeOwnerDashboardController 구현**

```kotlin
// cafeowner/controller/CafeOwnerDashboardController.kt
package com.blinddate.cafeowner.controller

import com.blinddate.auth.jwt.UserPrincipal
import com.blinddate.cafeowner.service.CafeOwnerService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/cafe-owner/dashboard")
class CafeOwnerDashboardController(private val cafeOwnerService: CafeOwnerService) {
    @GetMapping
    fun getDashboard(@AuthenticationPrincipal p: UserPrincipal) = cafeOwnerService.getDashboard(p.cafeId!!)
}
```

CafeOwnerService에 `getDashboard(cafeId)` 메서드 추가: 예정 이벤트 수, 수수료 요약 등.

- [x] **Step 3: Organizer/CafeOwner Notification 컨트롤러 구현**

```kotlin
// organizer/controller/OrganizerNotificationController.kt
package com.blinddate.organizer.controller

import com.blinddate.auth.jwt.UserPrincipal
import com.blinddate.notification.entity.RecipientType
import com.blinddate.notification.service.NotificationService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/organizer/notifications")
class OrganizerNotificationController(private val notificationService: NotificationService) {
    @GetMapping
    fun getNotifications(@AuthenticationPrincipal p: UserPrincipal) =
        notificationService.getNotifications(RecipientType.ORGANIZER, p.id)

    @PutMapping("/{id}/read")
    fun markAsRead(@AuthenticationPrincipal p: UserPrincipal, @PathVariable id: Long) =
        notificationService.markAsRead(id, RecipientType.ORGANIZER, p.id)
}
```

```kotlin
// cafeowner/controller/CafeOwnerNotificationController.kt
package com.blinddate.cafeowner.controller

import com.blinddate.auth.jwt.UserPrincipal
import com.blinddate.notification.entity.RecipientType
import com.blinddate.notification.service.NotificationService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/cafe-owner/notifications")
class CafeOwnerNotificationController(private val notificationService: NotificationService) {
    @GetMapping
    fun getNotifications(@AuthenticationPrincipal p: UserPrincipal) =
        notificationService.getNotifications(RecipientType.CAFE_OWNER, p.id)

    @PutMapping("/{id}/read")
    fun markAsRead(@AuthenticationPrincipal p: UserPrincipal, @PathVariable id: Long) =
        notificationService.markAsRead(id, RecipientType.CAFE_OWNER, p.id)
}
```

- [x] **Step 4: 빌드 확인**

Run: `cd backend && ./gradlew build`
Expected: BUILD SUCCESSFUL

- [x] **Step 5: 커밋**

```bash
git add -A && git commit -m "feat: add Dashboard and Notification controllers for Organizer and CafeOwner"
```

---

## Task 16: 구 패키지 삭제 + 최종 정리

모든 참조가 새 패키지로 이전되었으므로, 기존 bar/barowner 패키지를 삭제하고 설정 파일을 정리한다.

**Files:**
- Delete: `bar/` 전체, `barowner/` 전체, `auth/service/BarOwnerAuthService.kt`, `test/.../BarOwnerAuthServiceTest.kt`, `test/.../BarOwnerServiceTest.kt`
- Modify: `application-local.yml`, `application-test.yml`, `docker-compose.yml`

- [x] **Step 1: 구 bar/barowner 패키지 삭제**

```bash
rm -rf backend/src/main/kotlin/com/blinddate/bar/
rm -rf backend/src/main/kotlin/com/blinddate/barowner/
rm -rf backend/src/test/kotlin/com/blinddate/barowner/
rm -f backend/src/main/kotlin/com/blinddate/auth/service/BarOwnerAuthService.kt
rm -f backend/src/test/kotlin/com/blinddate/auth/service/BarOwnerAuthServiceTest.kt
```

- [x] **Step 2: 전체 빌드 확인 (구 패키지 삭제 후)**

Run: `cd backend && ./gradlew compileKotlin 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL (모든 참조가 이미 새 패키지로 이전됨)

빌드 실패 시: 남은 Bar/BarOwner 참조를 grep으로 찾아 수정.

```bash
grep -r "import com.blinddate.bar\." backend/src/ || echo "No remaining bar imports"
grep -r "import com.blinddate.barowner\." backend/src/ || echo "No remaining barowner imports"
```

- [x] **Step 3: application 설정 파일 정리**

테이블명 변경은 JPA `@Table(name = "cafe")` 어노테이션으로 처리되므로 설정 파일 변경 불필요할 수 있음. 확인 후 필요시 수정.

- [x] **Step 4: docker-compose.yml 정리**

불필요한 환경변수 확인/제거.

- [x] **Step 5: 전체 빌드 + 테스트 최종 확인**

Run: `cd backend && ./gradlew clean build`
Expected: BUILD SUCCESSFUL, 모든 테스트 PASS

- [x] **Step 6: 최종 커밋**

```bash
git add -A && git commit -m "chore: remove legacy bar/barowner packages and cleanup configs"
```

---

## 구현 순서 의존성

```
Task 1 (Cafe 엔티티 — 기존 bar 유지)
 └─▶ Task 2 (CafeOwner 엔티티 — 기존 barowner 유지)
      └─▶ Task 3 (Organizer 엔티티)
           └─▶ Task 4 (JWT/Security)
                ├─▶ Task 5 (Event + Application 엔티티 → cafe/organizer 참조)
                │    └─▶ Task 9 (Organizer 서비스/컨트롤러)
                ├─▶ Task 6 (ActionToken + Commission + Notification 엔티티)
                │    ├─▶ Task 10 (Commission 이원화 + Admin)
                │    └─▶ Task 11 (Notification + ActionToken 서비스)
                ├─▶ Task 7 (Auth 서비스)
                └─▶ Task 8 (Cafe/CafeOwner 서비스/컨트롤러)

Task 9~12 → Task 13 (전체 테스트 수정)
Task 13 → Task 14 (추가 컨트롤러)
Task 14 → Task 15 (Dashboard + Notification 컨트롤러)
Task 15 → Task 16 (구 패키지 삭제 + 최종 정리)
```
