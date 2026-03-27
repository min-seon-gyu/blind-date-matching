# Backend Marketplace - 제휴 마켓플레이스 구현 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 카페 주인과 주관자를 연결하는 양방향 제휴 마켓플레이스를 구현한다. Partnership(제휴) 엔티티와 MarketplacePost(마켓플레이스 글) 엔티티를 추가하고, 제휴 요청/수락/거절/해지 플로우와 마켓플레이스 CRUD API를 구현한다.

**Architecture:** partnership/ 패키지에 제휴 관리, marketplace/ 패키지에 글 관리를 분리한다. 마켓플레이스에서 제휴 요청 시 Partnership 엔티티를 생성하며, ACTIVE 상태의 Partnership이 있어야 주관자가 해당 카페에서 이벤트를 생성할 수 있다.

**Tech Stack:** Kotlin 1.9, Spring Boot 3.2, Spring Data JPA, Spring Security, MySQL 8.0, MockK

**Spec:** `docs/superpowers/specs/2026-03-23-frontend-design.md` (Partnership, MarketplacePost 엔티티, 마켓플레이스 API, 접근 제어)

---

## File Structure

### 신규 생성
```
backend/src/main/kotlin/com/blinddate/partnership/
├── entity/
│   ├── Partnership.kt
│   ├── PartnershipStatus.kt
│   └── PartnershipRequester.kt
├── repository/PartnershipRepository.kt
├── dto/PartnershipDtos.kt
├── service/PartnershipService.kt
└── controller/PartnershipController.kt      ← 제휴 요청 수락/거절은 여기 + cafeowner/organizer 컨트롤러

backend/src/main/kotlin/com/blinddate/marketplace/
├── entity/
│   ├── MarketplacePost.kt
│   ├── MarketplacePostType.kt
│   └── MarketplaceAuthorType.kt
├── repository/MarketplacePostRepository.kt
├── dto/MarketplaceDtos.kt
├── service/MarketplaceService.kt
└── controller/MarketplaceController.kt

backend/src/test/kotlin/com/blinddate/partnership/service/PartnershipServiceTest.kt
backend/src/test/kotlin/com/blinddate/marketplace/service/MarketplaceServiceTest.kt
```

### 수정 대상
```
backend/src/main/kotlin/com/blinddate/cafeowner/controller/CafeOwnerPartnershipController.kt  ← 신규
backend/src/main/kotlin/com/blinddate/organizer/controller/OrganizerPartnershipController.kt   ← 신규
backend/src/main/kotlin/com/blinddate/organizer/service/OrganizerService.kt                    ← 이벤트 생성 시 Partnership 검증 추가
backend/src/main/kotlin/com/blinddate/admin/controller/AdminPartnershipController.kt           ← 신규
backend/src/test/kotlin/com/blinddate/organizer/service/OrganizerServiceTest.kt                ← Partnership 검증 테스트 추가
```

---

## Task 1: Partnership 엔티티 + Enum + Repository

**Files:**
- Create: `partnership/entity/PartnershipStatus.kt`, `partnership/entity/PartnershipRequester.kt`, `partnership/entity/Partnership.kt`, `partnership/repository/PartnershipRepository.kt`

- [x] **Step 1: PartnershipStatus + PartnershipRequester enum 생성**

```kotlin
// partnership/entity/PartnershipStatus.kt
package com.blinddate.partnership.entity
enum class PartnershipStatus { PENDING, ACTIVE, TERMINATED }
```

```kotlin
// partnership/entity/PartnershipRequester.kt
package com.blinddate.partnership.entity
enum class PartnershipRequester { CAFE_OWNER, ORGANIZER }
```

- [x] **Step 2: Partnership 엔티티 생성**

```kotlin
// partnership/entity/Partnership.kt
package com.blinddate.partnership.entity

import com.blinddate.cafe.entity.Cafe
import com.blinddate.common.entity.BaseEntity
import com.blinddate.organizer.entity.Organizer
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "partnership")
class Partnership(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id", nullable = false)
    val cafe: Cafe,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    val organizer: Organizer,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PartnershipStatus = PartnershipStatus.PENDING,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val requestedBy: PartnershipRequester,

    @Column(columnDefinition = "TEXT")
    var message: String? = null,

    var respondedAt: LocalDateTime? = null,
    var terminatedAt: LocalDateTime? = null
) : BaseEntity()
```

- [x] **Step 3: PartnershipRepository 생성**

```kotlin
// partnership/repository/PartnershipRepository.kt
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
```

- [x] **Step 4: 빌드 확인**

Run: `cd backend && ./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

- [x] **Step 5: 커밋**

```bash
git add -A && git commit -m "feat: add Partnership entity, enums, and repository"
```

---

## Task 2: MarketplacePost 엔티티 + Enum + Repository

**Files:**
- Create: `marketplace/entity/MarketplacePostType.kt`, `marketplace/entity/MarketplaceAuthorType.kt`, `marketplace/entity/MarketplacePost.kt`, `marketplace/repository/MarketplacePostRepository.kt`

- [x] **Step 1: Enum 생성**

```kotlin
// marketplace/entity/MarketplacePostType.kt
package com.blinddate.marketplace.entity
enum class MarketplacePostType { OFFER_SPACE, SEEK_SPACE }
```

```kotlin
// marketplace/entity/MarketplaceAuthorType.kt
package com.blinddate.marketplace.entity
enum class MarketplaceAuthorType { CAFE_OWNER, ORGANIZER }
```

- [x] **Step 2: MarketplacePost 엔티티 생성**

```kotlin
// marketplace/entity/MarketplacePost.kt
package com.blinddate.marketplace.entity

import com.blinddate.common.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "marketplace_post")
class MarketplacePost(
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: MarketplacePostType,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val authorType: MarketplaceAuthorType,

    @Column(nullable = false)
    val authorId: Long,

    @Column(nullable = false)
    var title: String,

    @Column(columnDefinition = "TEXT", nullable = false)
    var description: String,

    @Column(nullable = false)
    var region: String,

    var capacity: Int? = null,
    var preferredDate: String? = null,

    @Column(columnDefinition = "TEXT")
    var imageUrls: String? = null,

    var cafeId: Long? = null,

    @Column(nullable = false)
    var isActive: Boolean = true
) : BaseEntity()
```

- [x] **Step 3: MarketplacePostRepository 생성**

```kotlin
// marketplace/repository/MarketplacePostRepository.kt
package com.blinddate.marketplace.repository

import com.blinddate.marketplace.entity.MarketplacePost
import com.blinddate.marketplace.entity.MarketplacePostType
import com.blinddate.marketplace.entity.MarketplaceAuthorType
import org.springframework.data.jpa.repository.JpaRepository

interface MarketplacePostRepository : JpaRepository<MarketplacePost, Long> {
    fun findByIsActiveTrueOrderByCreatedAtDesc(): List<MarketplacePost>
    fun findByTypeAndIsActiveTrueOrderByCreatedAtDesc(type: MarketplacePostType): List<MarketplacePost>
    fun findByRegionAndIsActiveTrueOrderByCreatedAtDesc(region: String): List<MarketplacePost>
    fun findByTypeAndRegionAndIsActiveTrueOrderByCreatedAtDesc(type: MarketplacePostType, region: String): List<MarketplacePost>
    fun findByAuthorTypeAndAuthorIdAndIsActiveTrueOrderByCreatedAtDesc(authorType: MarketplaceAuthorType, authorId: Long): List<MarketplacePost>
}
```

- [x] **Step 4: 빌드 확인**

Run: `cd backend && ./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

- [x] **Step 5: 커밋**

```bash
git add -A && git commit -m "feat: add MarketplacePost entity, enums, and repository"
```

---

## Task 3: PartnershipService (TDD)

**Files:**
- Create: `partnership/dto/PartnershipDtos.kt`, `partnership/service/PartnershipService.kt`
- Test: `test/.../partnership/service/PartnershipServiceTest.kt`

- [x] **Step 1: PartnershipDtos 생성**

```kotlin
// partnership/dto/PartnershipDtos.kt
package com.blinddate.partnership.dto

import com.blinddate.partnership.entity.Partnership
import com.blinddate.partnership.entity.PartnershipRequester
import com.blinddate.partnership.entity.PartnershipStatus

data class PartnershipResponse(
    val id: Long,
    val cafeId: Long,
    val cafeName: String,
    val organizerId: Long,
    val organizerName: String,
    val status: PartnershipStatus,
    val requestedBy: PartnershipRequester,
    val message: String?,
    val createdAt: String
) {
    companion object {
        fun from(p: Partnership) = PartnershipResponse(
            id = p.id, cafeId = p.cafe.id, cafeName = p.cafe.name,
            organizerId = p.organizer.id, organizerName = p.organizer.name,
            status = p.status, requestedBy = p.requestedBy,
            message = p.message, createdAt = p.createdAt.toString()
        )
    }
}

data class RequestPartnershipRequest(val message: String? = null)
```

- [x] **Step 2: PartnershipService 테스트 작성**

테스트 케이스:
1. 제휴 요청 성공 (ORGANIZER → CAFE_OWNER의 OFFER_SPACE 글)
2. 중복 제휴 요청 거부 (ACTIVE/PENDING 상태 이미 존재)
3. 제휴 수락 성공 (PENDING → ACTIVE)
4. 제휴 거절 성공 (PENDING → TERMINATED)
5. 제휴 해지 성공 (ACTIVE → TERMINATED)
6. 자기 글에 제휴 요청 거부

- [x] **Step 3: 테스트 실패 확인**

Run: `cd backend && ./gradlew test --tests "com.blinddate.partnership.service.*" 2>&1 | tail -5`
Expected: FAIL

- [x] **Step 4: PartnershipService 구현**

```kotlin
// partnership/service/PartnershipService.kt
package com.blinddate.partnership.service

import com.blinddate.cafe.repository.CafeRepository
import com.blinddate.cafeowner.repository.CafeOwnerRepository
import com.blinddate.common.exception.BadRequestException
import com.blinddate.common.exception.NotFoundException
import com.blinddate.marketplace.entity.MarketplaceAuthorType
import com.blinddate.marketplace.entity.MarketplacePost
import com.blinddate.marketplace.entity.MarketplacePostType
import com.blinddate.organizer.repository.OrganizerRepository
import com.blinddate.partnership.dto.PartnershipResponse
import com.blinddate.partnership.entity.*
import com.blinddate.partnership.repository.PartnershipRepository
import com.blinddate.auth.jwt.UserPrincipal
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class PartnershipService(
    private val partnershipRepository: PartnershipRepository,
    private val cafeRepository: CafeRepository,
    private val organizerRepository: OrganizerRepository,
    private val cafeOwnerRepository: CafeOwnerRepository
) {
    fun requestPartnership(principal: UserPrincipal, post: MarketplacePost, message: String?): PartnershipResponse {
        val (cafeId, organizerId, requester) = resolvePartnershipParties(principal, post)

        // 중복 제휴 체크 (ACTIVE 또는 PENDING)
        val existing = partnershipRepository.findByCafeIdAndOrganizerIdAndStatusIn(
            cafeId, organizerId, listOf(PartnershipStatus.ACTIVE, PartnershipStatus.PENDING)
        )
        if (existing != null) throw BadRequestException("이미 제휴 요청이 존재합니다")

        val cafe = cafeRepository.findById(cafeId).orElseThrow { NotFoundException("카페를 찾을 수 없습니다") }
        val organizer = organizerRepository.findById(organizerId).orElseThrow { NotFoundException("주관자를 찾을 수 없습니다") }

        val partnership = partnershipRepository.save(Partnership(
            cafe = cafe, organizer = organizer, requestedBy = requester, message = message
        ))
        return PartnershipResponse.from(partnership)
    }

    fun accept(partnershipId: Long, actorId: Long): PartnershipResponse {
        val partnership = getById(partnershipId)
        if (partnership.status != PartnershipStatus.PENDING) throw BadRequestException("대기 중인 요청만 수락할 수 있습니다")
        partnership.status = PartnershipStatus.ACTIVE
        partnership.respondedAt = LocalDateTime.now()
        return PartnershipResponse.from(partnershipRepository.save(partnership))
    }

    fun reject(partnershipId: Long, actorId: Long): PartnershipResponse {
        val partnership = getById(partnershipId)
        if (partnership.status != PartnershipStatus.PENDING) throw BadRequestException("대기 중인 요청만 거절할 수 있습니다")
        partnership.status = PartnershipStatus.TERMINATED
        partnership.respondedAt = LocalDateTime.now()
        return PartnershipResponse.from(partnershipRepository.save(partnership))
    }

    fun terminate(partnershipId: Long, actorId: Long): PartnershipResponse {
        val partnership = getById(partnershipId)
        if (partnership.status != PartnershipStatus.ACTIVE) throw BadRequestException("활성 제휴만 해지할 수 있습니다")
        partnership.status = PartnershipStatus.TERMINATED
        partnership.terminatedAt = LocalDateTime.now()
        return PartnershipResponse.from(partnershipRepository.save(partnership))
    }

    fun getByCafeId(cafeId: Long): List<PartnershipResponse> =
        partnershipRepository.findByCafeId(cafeId).map { PartnershipResponse.from(it) }

    fun getByOrganizerId(organizerId: Long): List<PartnershipResponse> =
        partnershipRepository.findByOrganizerId(organizerId).map { PartnershipResponse.from(it) }

    fun getAll(): List<PartnershipResponse> =
        partnershipRepository.findAll().map { PartnershipResponse.from(it) }

    fun hasActivePartnership(cafeId: Long, organizerId: Long): Boolean =
        partnershipRepository.findByCafeIdAndOrganizerIdAndStatusIn(
            cafeId, organizerId, listOf(PartnershipStatus.ACTIVE)
        ) != null

    private fun getById(id: Long): Partnership =
        partnershipRepository.findById(id).orElseThrow { NotFoundException("제휴를 찾을 수 없습니다") }

    private fun resolvePartnershipParties(principal: UserPrincipal, post: MarketplacePost): Triple<Long, Long, PartnershipRequester> {
        return when {
            // ORGANIZER가 OFFER_SPACE 글에 요청
            principal.userType == com.blinddate.auth.jwt.UserType.ORGANIZER && post.type == MarketplacePostType.OFFER_SPACE -> {
                if (post.authorType == MarketplaceAuthorType.ORGANIZER && post.authorId == principal.id)
                    throw BadRequestException("자기 글에 제휴 요청할 수 없습니다")
                Triple(post.cafeId!!, principal.id, PartnershipRequester.ORGANIZER)
            }
            // CAFE_OWNER가 SEEK_SPACE 글에 요청
            principal.userType == com.blinddate.auth.jwt.UserType.CAFE_OWNER && post.type == MarketplacePostType.SEEK_SPACE -> {
                if (post.authorType == MarketplaceAuthorType.CAFE_OWNER && post.authorId == principal.id)
                    throw BadRequestException("자기 글에 제휴 요청할 수 없습니다")
                Triple(principal.cafeId!!, post.authorId, PartnershipRequester.CAFE_OWNER)
            }
            else -> throw BadRequestException("상대 역할의 글에만 제휴 요청할 수 있습니다")
        }
    }
}
```

- [x] **Step 5: 테스트 통과 확인**

Run: `cd backend && ./gradlew test --tests "com.blinddate.partnership.service.*" 2>&1 | tail -5`
Expected: PASS

- [x] **Step 6: 커밋**

```bash
git add -A && git commit -m "feat: add PartnershipService with TDD (request, accept, reject, terminate)"
```

---

## Task 4: MarketplaceService (TDD)

**Files:**
- Create: `marketplace/dto/MarketplaceDtos.kt`, `marketplace/service/MarketplaceService.kt`
- Test: `test/.../marketplace/service/MarketplaceServiceTest.kt`

- [x] **Step 1: MarketplaceDtos 생성**

```kotlin
// marketplace/dto/MarketplaceDtos.kt
package com.blinddate.marketplace.dto

import com.blinddate.marketplace.entity.MarketplacePost
import com.blinddate.marketplace.entity.MarketplacePostType
import com.blinddate.marketplace.entity.MarketplaceAuthorType

data class CreateMarketplacePostRequest(
    val title: String,
    val description: String,
    val region: String,
    val capacity: Int? = null,
    val preferredDate: String? = null,
    val imageUrls: List<String>? = null,
    val cafeId: Long? = null
)

data class UpdateMarketplacePostRequest(
    val title: String? = null,
    val description: String? = null,
    val region: String? = null,
    val capacity: Int? = null,
    val preferredDate: String? = null,
    val imageUrls: List<String>? = null
)

data class MarketplacePostResponse(
    val id: Long,
    val type: MarketplacePostType,
    val authorType: MarketplaceAuthorType,
    val authorId: Long,
    val authorName: String,
    val title: String,
    val description: String,
    val region: String,
    val capacity: Int?,
    val preferredDate: String?,
    val imageUrls: List<String>,
    val cafeId: Long?,
    val cafeAddress: String?,
    val cafeLatitude: Double?,
    val cafeLongitude: Double?,
    val isActive: Boolean,
    val createdAt: String
)
```

- [x] **Step 2: MarketplaceService 테스트 작성**

테스트 케이스:
1. CAFE_OWNER가 OFFER_SPACE 글 작성 성공
2. ORGANIZER가 SEEK_SPACE 글 작성 성공
3. CAFE_OWNER가 SEEK_SPACE 글 작성 시 거부
4. 작성자 본인만 글 수정 가능
5. 작성자 본인만 글 삭제 가능 (soft delete: isActive=false)
6. 글 목록 조회 (type 필터, region 필터)

- [x] **Step 3: 테스트 실패 확인**

- [x] **Step 4: MarketplaceService 구현**

주요 로직:
- `createPost(principal, request)`: authorType/type 매칭 검증, CAFE_OWNER이면 cafeId를 principal.cafeId에서 설정
- `updatePost(principal, postId, request)`: 작성자 본인 검증
- `deletePost(principal, postId)`: isActive = false (soft delete)
- `getPosts(type?, region?)`: 필터링 조회
- `getPost(postId)`: 상세 조회 (authorName 포함, Cafe 정보 포함)
- `getMyPosts(principal)`: 내 글 목록

authorName 조회: authorType에 따라 CafeOwnerRepository 또는 OrganizerRepository에서 이름 조회.

- [x] **Step 5: 테스트 통과 확인**

- [x] **Step 6: 커밋**

```bash
git add -A && git commit -m "feat: add MarketplaceService with TDD (CRUD, access control)"
```

---

## Task 5: Marketplace 컨트롤러

**Files:**
- Create: `marketplace/controller/MarketplaceController.kt`

- [x] **Step 1: MarketplaceController 구현**

```kotlin
// marketplace/controller/MarketplaceController.kt
package com.blinddate.marketplace.controller

import com.blinddate.auth.jwt.UserPrincipal
import com.blinddate.marketplace.dto.CreateMarketplacePostRequest
import com.blinddate.marketplace.dto.UpdateMarketplacePostRequest
import com.blinddate.marketplace.entity.MarketplacePostType
import com.blinddate.marketplace.service.MarketplaceService
import com.blinddate.partnership.dto.RequestPartnershipRequest
import com.blinddate.partnership.service.PartnershipService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/marketplace/posts")
class MarketplaceController(
    private val marketplaceService: MarketplaceService,
    private val partnershipService: PartnershipService
) {
    @GetMapping
    fun getPosts(
        @RequestParam type: MarketplacePostType?,
        @RequestParam region: String?
    ) = marketplaceService.getPosts(type, region)

    @PostMapping
    fun createPost(
        @AuthenticationPrincipal p: UserPrincipal,
        @RequestBody request: CreateMarketplacePostRequest
    ) = marketplaceService.createPost(p, request)

    @GetMapping("/{id}")
    fun getPost(@PathVariable id: Long) = marketplaceService.getPost(id)

    @PutMapping("/{id}")
    fun updatePost(
        @AuthenticationPrincipal p: UserPrincipal,
        @PathVariable id: Long,
        @RequestBody request: UpdateMarketplacePostRequest
    ) = marketplaceService.updatePost(p, id, request)

    @DeleteMapping("/{id}")
    fun deletePost(
        @AuthenticationPrincipal p: UserPrincipal,
        @PathVariable id: Long
    ) = marketplaceService.deletePost(p, id)

    @PostMapping("/{id}/request-partnership")
    fun requestPartnership(
        @AuthenticationPrincipal p: UserPrincipal,
        @PathVariable id: Long,
        @RequestBody(required = false) request: RequestPartnershipRequest?
    ): Any {
        val post = marketplaceService.getPostEntity(id)
        return partnershipService.requestPartnership(p, post, request?.message)
    }
}
```

- [x] **Step 2: 빌드 확인**

Run: `cd backend && ./gradlew compileKotlin`

- [x] **Step 3: 커밋**

```bash
git add -A && git commit -m "feat: add MarketplaceController with CRUD and partnership request"
```

---

## Task 6: Partnership 관리 컨트롤러 (CafeOwner, Organizer, Admin)

**Files:**
- Create: `cafeowner/controller/CafeOwnerPartnershipController.kt`
- Create: `organizer/controller/OrganizerPartnershipController.kt`
- Create: `admin/controller/AdminPartnershipController.kt`

- [x] **Step 1: CafeOwnerPartnershipController 구현**

```kotlin
package com.blinddate.cafeowner.controller

import com.blinddate.auth.jwt.UserPrincipal
import com.blinddate.partnership.service.PartnershipService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/cafe-owner/partnerships")
class CafeOwnerPartnershipController(private val partnershipService: PartnershipService) {
    @GetMapping
    fun getPartnerships(@AuthenticationPrincipal p: UserPrincipal) =
        partnershipService.getByCafeId(p.cafeId!!)

    @PutMapping("/{id}/accept")
    fun accept(@AuthenticationPrincipal p: UserPrincipal, @PathVariable id: Long) =
        partnershipService.accept(id, p.id)

    @PutMapping("/{id}/reject")
    fun reject(@AuthenticationPrincipal p: UserPrincipal, @PathVariable id: Long) =
        partnershipService.reject(id, p.id)

    @PutMapping("/{id}/terminate")
    fun terminate(@AuthenticationPrincipal p: UserPrincipal, @PathVariable id: Long) =
        partnershipService.terminate(id, p.id)
}
```

- [x] **Step 2: OrganizerPartnershipController 구현**

```kotlin
package com.blinddate.organizer.controller

import com.blinddate.auth.jwt.UserPrincipal
import com.blinddate.partnership.service.PartnershipService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/organizer/partnerships")
class OrganizerPartnershipController(private val partnershipService: PartnershipService) {
    @GetMapping
    fun getPartnerships(@AuthenticationPrincipal p: UserPrincipal) =
        partnershipService.getByOrganizerId(p.id)
}
```

- [x] **Step 3: AdminPartnershipController 구현**

```kotlin
package com.blinddate.admin.controller

import com.blinddate.partnership.service.PartnershipService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/partnerships")
class AdminPartnershipController(private val partnershipService: PartnershipService) {
    @GetMapping
    fun getAll() = partnershipService.getAll()
}
```

- [x] **Step 4: 빌드 확인**

Run: `cd backend && ./gradlew compileKotlin`

- [x] **Step 5: 커밋**

```bash
git add -A && git commit -m "feat: add Partnership management controllers (CafeOwner, Organizer, Admin)"
```

---

## Task 7: OrganizerService에 Partnership 검증 추가

이벤트 생성 시 해당 카페와 ACTIVE 상태 Partnership이 있는지 검증한다.

**Files:**
- Modify: `organizer/service/OrganizerService.kt`
- Test: `test/.../organizer/service/OrganizerServiceTest.kt`

- [x] **Step 1: OrganizerServiceTest에 Partnership 검증 테스트 추가**

테스트 케이스:
1. ACTIVE Partnership 있을 때 이벤트 생성 성공
2. Partnership 없을 때 이벤트 생성 실패 ("먼저 카페와 제휴를 맺어주세요")

- [x] **Step 2: 테스트 실패 확인**

- [x] **Step 3: OrganizerService.createEvent에 Partnership 검증 추가**

```kotlin
// OrganizerService.createEvent 시작 부분에 추가:
if (!partnershipService.hasActivePartnership(request.cafeId, organizerId)) {
    throw BadRequestException("먼저 카페와 제휴를 맺어주세요")
}
```

OrganizerService 생성자에 `PartnershipService` 의존성 추가.

- [x] **Step 4: 테스트 통과 확인**

Run: `cd backend && ./gradlew test --tests "com.blinddate.organizer.service.*" 2>&1 | tail -5`
Expected: PASS

- [x] **Step 5: 커밋**

```bash
git add -A && git commit -m "feat: add Partnership validation on event creation"
```

---

## Task 8: 전체 빌드 + 테스트 확인

- [x] **Step 1: 전체 빌드**

Run: `cd backend && ./gradlew clean build`
Expected: BUILD SUCCESSFUL, 모든 테스트 PASS

- [x] **Step 2: 최종 커밋**

```bash
git add -A && git commit -m "chore: verify marketplace feature complete - all tests pass"
```

---

## 구현 순서 의존성

```
Task 1 (Partnership 엔티티)
 └─▶ Task 3 (PartnershipService)
      └─▶ Task 6 (Partnership 컨트롤러)
      └─▶ Task 7 (이벤트 생성 시 Partnership 검증)

Task 2 (MarketplacePost 엔티티)
 └─▶ Task 4 (MarketplaceService)
      └─▶ Task 5 (MarketplaceController — PartnershipService도 필요)

Task 5, 6, 7 → Task 8 (최종 확인)
```
