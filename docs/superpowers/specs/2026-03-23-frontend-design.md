# B2B 소개팅 매칭 플랫폼 - 풀스택 재설계 문서

## 개요

B2B 멀티테넌트 소개팅 매칭 플랫폼의 역할 구조를 재설계하고, 이에 맞춰 백엔드 API를 변경하고 프론트엔드를 전면 구현한다.

### 핵심 변경: 역할 분리

기존 BarOwner(바 사장님)가 장소 제공과 이벤트 운영을 모두 담당하던 구조를 분리한다.

| 기존 | 변경 | 역할 |
|------|------|------|
| BarOwner | **CafeOwner (카페 주인)** | 장소 제공, 마켓플레이스 글 등록 |
| _(없음)_ | **Organizer (주관자)** | 이벤트 기획/운영, 신청자 관리, 매칭 |
| Participant | **Participant (참가자)** | 이벤트 신청, 선택, 결과 확인 |
| PlatformAdmin | **PlatformAdmin** | 전체 관리, 양쪽 수수료 관리 |

### 신규 기능: 제휴 마켓플레이스

카페 주인과 주관자를 연결하는 양방향 마켓플레이스.

- 카페 주인: "장소를 제공합니다" 글 등록
- 주관자: "장소를 찾습니다" 글 등록
- 일회성 또는 지속적 파트너십 모두 가능 (N:N)
- 제휴 성사 후 주관자가 해당 카페에서 이벤트 생성 가능

### 네이밍 규칙

내부 모델(엔티티, DB 테이블, API 경로)과 UI 표시명을 통일한다.

| 기존 | 변경 | 비고 |
|------|------|------|
| `Bar` | `Cafe` | 엔티티, 테이블, API 경로 모두 변경 |
| `BarOwner` | `CafeOwner` | 엔티티, 테이블, API 경로 모두 변경 |
| `/bars/{slug}` | `/cafes/{slug}` | URL 경로 변경 |
| `barId` | `cafeId` | FK, JWT claim 변경 |

스터디 프로젝트이므로 운영 데이터 마이그레이션은 불필요. 깨끗하게 리네이밍한다.

---

## 1. 백엔드 변경 사항

### 1.1 엔티티 변경

#### 리네이밍 (기존 → 변경)

**Bar → Cafe**
```
Cafe
 ├── id: Long (PK)
 ├── name: String
 ├── address: String
 ├── description: String (TEXT)
 ├── logoUrl: String?
 ├── coverImageUrl: String?
 ├── slug: String (unique)
 ├── latitude: Double?              ← 신규: 카카오맵 좌표
 ├── longitude: Double?             ← 신규: 카카오맵 좌표
 ├── commissionRate: Int (기본 10)
 ├── isActive: Boolean (기본 true)
 ├── createdAt: LocalDateTime
 └── updatedAt: LocalDateTime
```

**BarOwner → CafeOwner**
```
CafeOwner
 ├── id: Long (PK)
 ├── cafe: Cafe (N:1)              ← 기존 bar → cafe
 ├── name: String
 ├── phoneNumber: String
 ├── email: String (unique)
 ├── kakaoId: String?
 ├── password: String (bcrypt)
 ├── createdAt: LocalDateTime
 └── updatedAt: LocalDateTime
```

#### 신규 엔티티

**Organizer (주관자)**
```
Organizer
 ├── id: Long (PK)
 ├── name: String
 ├── phoneNumber: String
 ├── email: String (unique)
 ├── kakaoId: String?              ← 카톡 알림 수신용
 ├── password: String (bcrypt)
 ├── description: String? (TEXT)   ← 자기소개 (마켓플레이스용)
 ├── commissionRate: Int (기본 15) ← 주관자별 수수료율 (%)
 ├── createdAt: LocalDateTime
 └── updatedAt: LocalDateTime
```

**Partnership (제휴)**
```
Partnership
 ├── id: Long (PK)
 ├── cafe: Cafe (N:1)
 ├── organizer: Organizer (N:1)
 ├── status: PartnershipStatus (PENDING/ACTIVE/TERMINATED)
 ├── requestedBy: PartnershipRequester (CAFE_OWNER/ORGANIZER)
 ├── message: String? (TEXT)       ← 제휴 요청 메시지
 ├── respondedAt: LocalDateTime?
 ├── terminatedAt: LocalDateTime?
 ├── createdAt: LocalDateTime
 └── updatedAt: LocalDateTime

 UniqueConstraint: (cafe_id, organizer_id) WHERE status != 'TERMINATED'
   ← ACTIVE/PENDING 상태에서만 중복 방지. TERMINATED 후 재제휴 시 새 레코드 생성.
   ← DB 레벨에서 partial unique index 미지원 시, 서비스 레이어에서 ACTIVE/PENDING 상태 존재 여부 체크
```

**MarketplacePost (마켓플레이스 글)**
```
MarketplacePost
 ├── id: Long (PK)
 ├── type: MarketplacePostType (OFFER_SPACE/SEEK_SPACE)
 ├── authorType: MarketplaceAuthorType (CAFE_OWNER/ORGANIZER)
 ├── authorId: Long                ← CafeOwner.id 또는 Organizer.id
 ├── title: String
 ├── description: String (TEXT)
 ├── region: String                ← 지역 (예: "강남", "홍대")
 ├── capacity: Int?                ← 수용 인원 (OFFER_SPACE일 때)
 ├── preferredDate: String?        ← 희망 날짜/요일
 ├── imageUrls: String? (TEXT)     ← 쉼표 구분 URL 목록
 ├── cafeId: Long?                 ← OFFER_SPACE일 때 Cafe.id 참조
 ├── isActive: Boolean (기본 true)
 ├── createdAt: LocalDateTime
 └── updatedAt: LocalDateTime
```

#### 기존 엔티티 수정

**Event** — organizerId 추가
```diff
Event
  ├── cafe: Cafe (N:1)             ← bar → cafe
+ ├── organizer: Organizer (N:1)   ← 이벤트 운영 주체
  ├── title: String
  ├── ... (나머지 필드 동일)
```

**Application** — reviewedBy 타입 변경
```diff
Application
- ├── reviewedBy: BarOwner? (N:1)
+ ├── reviewedBy: Organizer? (N:1)  ← 주관자가 승인/거절
```

**ActionToken** — barOwnerId를 범용화
```diff
ActionToken
- ├── barOwnerId: Long
+ ├── actorType: UserType           ← ORGANIZER 또는 CAFE_OWNER
+ ├── actorId: Long                 ← 해당 역할의 사용자 ID
```

ActionToken actionType 목록:
- `APPROVE_APPLICATION` — 주관자가 신청 승인 (targetId: Application.id)
- `REJECT_APPLICATION` — 주관자가 신청 거절 (targetId: Application.id)
- `ACCEPT_PARTNERSHIP` — 카페 주인/주관자가 제휴 수락 (targetId: Partnership.id)
- `REJECT_PARTNERSHIP` — 카페 주인/주관자가 제휴 거절 (targetId: Partnership.id)

**Commission** — 이원화 (이벤트당 2건)
```diff
Commission
- ├── bar: Bar (N:1)
- ├── event: Event (1:1)
+ ├── cafe: Cafe (N:1)
+ ├── event: Event (N:1)            ← 1:1 → N:1 (이벤트당 2건)
+ ├── targetType: CommissionTargetType (ORGANIZER/CAFE_OWNER)
+ ├── targetId: Long                ← Organizer.id 또는 CafeOwner.id
  ├── participantCount: Int
  ├── eventPrice: Int
  ├── commissionRate: Int
  ├── unitPrice: Int
  ├── totalAmount: Int
  ├── status: CommissionStatus
  ├── invoicedAt: LocalDateTime?
  └── paidAt: LocalDateTime?
```

이벤트 COMPLETED 시 Commission 2건 자동 생성:
- 주관자용: 이벤트 수수료 (참가자 수 × 이벤트 참가비 × Organizer.commissionRate / 100)
- 카페용: 중개 수수료 (참가자 수 × 이벤트 참가비 × Cafe.commissionRate / 100)
- 각 Commission 레코드의 commissionRate 필드에 생성 시점의 수수료율 스냅샷 저장

**Notification** — RecipientType 확장
```diff
RecipientType
  PARTICIPANT
- BAR_OWNER
+ CAFE_OWNER
+ ORGANIZER
```

### 1.2 Enum 변경

```diff
UserType
  PARTICIPANT
- BAR_OWNER
+ CAFE_OWNER
+ ORGANIZER
  PLATFORM_ADMIN

+ PartnershipStatus
+   PENDING, ACTIVE, TERMINATED

+ PartnershipRequester
+   CAFE_OWNER, ORGANIZER

+ MarketplacePostType
+   OFFER_SPACE, SEEK_SPACE

+ MarketplaceAuthorType
+   CAFE_OWNER, ORGANIZER

+ CommissionTargetType
+   ORGANIZER, CAFE_OWNER

NotificationType
  NEW_APPLICATION
  APPROVED
  REJECTED
  MATCH_RESULT
  EVENT_REMINDER
  CHOICE_REMINDER
  EVENT_COMPLETED
  COMMISSION_INVOICE
+ PARTNERSHIP_REQUESTED    ← 제휴 요청 수신
+ PARTNERSHIP_ACCEPTED     ← 제휴 수락
+ PARTNERSHIP_REJECTED     ← 제휴 거절
```

### 1.3 JWT 변경

```json
{
  "sub": "123",
  "userType": "PARTICIPANT | ORGANIZER | CAFE_OWNER | PLATFORM_ADMIN",
  "cafeId": 456
}
```

- `cafeId`: CAFE_OWNER일 때만 포함
- ORGANIZER는 cafeId 없음 (여러 카페와 제휴 가능하므로)

### 1.4 SecurityConfig 변경

```
공개 (인증 불필요):
  /api/auth/**
  /api/organizer/auth/**
  /api/cafe-owner/auth/**
  /api/admin/auth/**
  GET /api/cafes/**
  /api/actions/**

주관자 전용 (ORGANIZER):
  /api/organizer/**

카페 주인 전용 (CAFE_OWNER):
  /api/cafe-owner/**

관리자 전용 (PLATFORM_ADMIN):
  /api/admin/**

마켓플레이스 (ORGANIZER 또는 CAFE_OWNER):
  /api/marketplace/**

인증 필요 (PARTICIPANT):
  나머지 모든 경로
```

### 1.5 API 변경 전체 매핑

#### 기존 유지 (경로 변경 없음)

```
POST   /api/auth/kakao                     ← 참가자 카카오 로그인
POST   /api/auth/refresh                   ← 토큰 갱신 (모든 사용자 유형 공용)

GET    /api/me                             ← 참가자 내 정보
POST   /api/me/profile                     ← 프로필 생성
PUT    /api/me/profile                     ← 프로필 수정
GET    /api/me/applications                ← 내 신청 내역
GET    /api/me/notifications               ← 알림 목록
PUT    /api/notifications/{id}/read        ← 읽음 처리

POST   /api/events/{id}/apply              ← 이벤트 신청
DELETE /api/events/{id}/apply              ← 신청 취소
GET    /api/events/{id}/participants       ← 이성 참가자 목록
POST   /api/events/{id}/choices            ← 선택 제출
PUT    /api/events/{id}/choices            ← 선택 변경
GET    /api/events/{id}/result             ← 매칭 결과

GET    /api/actions/{token}                ← 액션 정보
POST   /api/actions/{token}/execute        ← 액션 실행

POST   /api/upload/presigned-url           ← S3 업로드 URL
```

#### 리네이밍 (bar → cafe)

```
기존                              → 변경
GET /api/bars/{slug}              → GET /api/cafes/{slug}
GET /api/bars/{slug}/events       → GET /api/cafes/{slug}/events
GET /api/bars/{slug}/events/{id}  → GET /api/cafes/{slug}/events/{id}
```

#### BarOwner → 역할 분리

```
기존 (삭제)                                    → 대체
POST /api/bar-owner/auth/login                → POST /api/cafe-owner/auth/login
                                              + POST /api/organizer/auth/login
GET  /api/bar-owner/my-bar                    → GET  /api/cafe-owner/my-cafe
PUT  /api/bar-owner/my-bar                    → PUT  /api/cafe-owner/my-cafe
POST /api/bar-owner/events                    → POST /api/organizer/events
PUT  /api/bar-owner/events/{id}               → PUT  /api/organizer/events/{id}
DELETE /api/bar-owner/events/{id}             → DELETE /api/organizer/events/{id}
GET  /api/bar-owner/events                    → GET  /api/organizer/events
PUT  /api/bar-owner/events/{id}/close         → PUT  /api/organizer/events/{id}/close
GET  /api/bar-owner/events/{id}/applications  → GET  /api/organizer/events/{id}/applications
PUT  /api/bar-owner/applications/{id}/approve → PUT  /api/organizer/applications/{id}/approve
PUT  /api/bar-owner/applications/{id}/reject  → PUT  /api/organizer/applications/{id}/reject
```

#### 신규 API — 주관자

```
POST   /api/organizer/auth/login                  ← 이메일/비밀번호 로그인
GET    /api/organizer/dashboard                    ← 대시보드
GET    /api/organizer/partnerships                 ← 제휴 카페 목록
GET    /api/organizer/events/{id}/stats            ← 이벤트 결과 통계
GET    /api/organizer/commissions                  ← 수수료 내역
GET    /api/organizer/notifications                ← 알림 목록
PUT    /api/organizer/notifications/{id}/read      ← 알림 읽음
```

주관자 이벤트 생성 시 `cafeId`를 필수 파라미터로 전달. 해당 카페와 ACTIVE 상태 제휴가 있어야 생성 가능.

#### 신규 API — 카페 주인

```
POST   /api/cafe-owner/auth/login                  ← 이메일/비밀번호 로그인
GET    /api/cafe-owner/my-cafe                     ← 내 카페 정보
PUT    /api/cafe-owner/my-cafe                     ← 카페 정보 수정 (위치 좌표 포함)
GET    /api/cafe-owner/dashboard                   ← 대시보드
GET    /api/cafe-owner/partnerships                ← 제휴 관리
PUT    /api/cafe-owner/partnerships/{id}/accept    ← 제휴 수락
PUT    /api/cafe-owner/partnerships/{id}/reject    ← 제휴 거절
PUT    /api/cafe-owner/partnerships/{id}/terminate ← 제휴 해지
GET    /api/cafe-owner/events                      ← 내 카페 이벤트 (읽기 전용, 모든 주관자의 이벤트 포함. 제휴 해지 후에도 과거 이벤트는 표시)
GET    /api/cafe-owner/commissions                 ← 수수료 내역
GET    /api/cafe-owner/notifications               ← 알림 목록
PUT    /api/cafe-owner/notifications/{id}/read     ← 알림 읽음
```

#### 신규 API — 마켓플레이스

```
GET    /api/marketplace/posts                      ← 글 목록 (필터: type, region)
POST   /api/marketplace/posts                      ← 글 작성
GET    /api/marketplace/posts/{id}                 ← 글 상세
PUT    /api/marketplace/posts/{id}                 ← 글 수정
DELETE /api/marketplace/posts/{id}                 ← 글 삭제
POST   /api/marketplace/posts/{id}/request-partnership ← 제휴 요청
```

마켓플레이스 접근 제어:
- 글 목록/상세 조회: ORGANIZER 또는 CAFE_OWNER (로그인 필수)
- 글 작성: 본인 authorType에 맞는 type만 (CAFE_OWNER → OFFER_SPACE, ORGANIZER → SEEK_SPACE)
- 글 수정/삭제: 작성자 본인만
- 제휴 요청: 상대 역할만 (ORGANIZER가 OFFER_SPACE 글에 요청, CAFE_OWNER가 SEEK_SPACE 글에 요청)
- 제휴 요청 시 Partnership 생성 규칙:
  - ORGANIZER → OFFER_SPACE 글: cafeId는 글의 cafeId에서 추론, organizerId는 JWT에서 추출
  - CAFE_OWNER → SEEK_SPACE 글: cafeId는 JWT의 cafeId에서 추출, organizerId는 글의 authorId에서 추론
  - 자기 글에 자기가 요청하는 것은 서버에서 거부

#### 관리자 API 변경

```
기존 (삭제)                        → 변경
POST /api/admin/bars              → POST /api/admin/cafes
GET  /api/admin/bars              → GET  /api/admin/cafes
POST /api/admin/bar-owners        → POST /api/admin/cafe-owners

신규:
POST   /api/admin/organizers                       ← 주관자 계정 생성
GET    /api/admin/organizers                       ← 주관자 목록
GET    /api/admin/partnerships                     ← 전체 제휴 현황
GET    /api/admin/commissions                      ← 전체 수수료 (targetType 필터)
PUT    /api/admin/commissions/{id}/invoice          ← 청구
PUT    /api/admin/commissions/{id}/paid             ← 입금 확인
GET    /api/admin/dashboard                        ← 대시보드 (통계 확장)
```

계정 생성 정책: 카페 주인, 주관자 모두 플랫폼 관리자가 등록. 셀프 가입 없음.

### 1.6 알림 시스템 변경

| 수신자 | 시점 | 메시지 | 카톡 발송 |
|--------|------|--------|----------|
| 주관자 | 새 신청 | "홍길동(28/남) 님이 금요일 이벤트에 신청" | O (ActionToken: 승인/거절) |
| 주관자 | 이벤트 완료 | "이벤트 완료! 참가자 16명, 매칭 5쌍" | O |
| 주관자 | 수수료 청구 | "3월 수수료 12만원 청구" | O |
| 카페 주인 | 제휴 요청 | "주관자 김OO님이 제휴를 요청" | O (ActionToken: 수락/거절) |
| 카페 주인 | 이벤트 예정 | "내일 오후 8시 이벤트 예정 (16명)" | O |
| 카페 주인 | 수수료 청구 | "3월 중개 수수료 5만원 청구" | O |
| 참가자 | 승인/거절/매칭 | (기존과 동일) | O |

### 1.7 스케줄러 변경

- **EventStatusScheduler**: `bar` → `cafe` 참조 변경
- **MatchingScheduler**: `bar` → `cafe`, `barOwner` → `organizer` 참조 변경. 매칭 완료 알림을 주관자에게 발송
- **ReminderScheduler**: 참가자 + 카페 주인(이벤트 전날) 알림 발송

### 1.8 백엔드 패키지 구조 변경

```
backend/src/main/kotlin/com/blinddate/
├── common/ (기존 유지)
├── auth/ (UserType 확장, OrganizerAuthService + CafeOwnerAuthService 추가)
├── participant/ (기존 유지)
├── cafe/                    ← bar/ 리네이밍
│   ├── entity/Cafe.kt
│   ├── repository/
│   ├── controller/CafeController.kt (공개 API)
│   ├── dto/
│   └── service/
├── cafeowner/               ← barowner/ 리네이밍 + 역할 축소
│   ├── entity/CafeOwner.kt
│   ├── repository/
│   ├── controller/ (auth, my-cafe, partnerships, events 읽기전용, commissions)
│   ├── dto/
│   └── service/
├── organizer/               ← 신규
│   ├── entity/Organizer.kt
│   ├── repository/
│   ├── controller/ (auth, events CRUD, applications, stats, commissions)
│   ├── dto/
│   └── service/
├── marketplace/             ← 신규
│   ├── entity/MarketplacePost.kt
│   ├── repository/
│   ├── controller/
│   ├── dto/
│   └── service/
├── partnership/             ← 신규
│   ├── entity/Partnership.kt
│   ├── repository/
│   ├── controller/ (제휴 요청은 마켓플레이스 컨트롤러에서 처리)
│   ├── dto/
│   └── service/
├── event/ (bar → cafe 참조 변경, organizerId 추가)
├── application/ (reviewedBy: BarOwner → Organizer)
├── matching/ (기존 유지)
├── notification/ (RecipientType 확장, ORGANIZER/CAFE_OWNER 지원)
├── action/ (barOwnerId → actorType + actorId)
├── commission/ (targetType + targetId 추가, 이원화)
├── admin/ (cafe/organizer 관리 추가)
└── scheduler/ (bar → cafe, barOwner → organizer)
```

---

## 2. 프론트엔드 설계

### 2.1 기술 스택

| 구분 | 선택 | 이유 |
|------|------|------|
| 프레임워크 | React 19 + TypeScript | 기존 유지 |
| 빌드 | Vite 8 | 기존 유지 |
| UI | shadcn/ui + Tailwind CSS | 참가자: Tailwind 커스텀, 관리자: shadcn 활용 |
| 서버 상태 | @tanstack/react-query | 기존 유지 |
| 클라이언트 상태 | zustand | 인증/UI 상태 |
| 라우팅 | react-router-dom | 기존 유지 |
| HTTP | axios | 기존 유지 |
| 지도 | 카카오맵 JavaScript SDK + 카카오 로컬 API + 카카오 모빌리티 API | 카카오 생태계 통일 |

### 2.2 프로젝트 구조

단일 앱 + 라우팅 분리.

```
frontend/src/
├── api/
│   ├── client.ts              ← axios 인스턴스 + 인터셉터
│   ├── auth.ts
│   ├── participant.ts
│   ├── organizer.ts
│   ├── cafeOwner.ts
│   ├── marketplace.ts
│   ├── admin.ts
│   └── upload.ts
├── components/
│   ├── common/                ← Button, Modal, Map, EmptyState, ErrorBoundary
│   ├── participant/
│   ├── organizer/
│   ├── cafe-owner/
│   ├── admin/
│   └── marketplace/
├── pages/
│   ├── participant/
│   ├── organizer/
│   ├── cafe-owner/
│   ├── admin/
│   └── marketplace/
├── hooks/
│   ├── useAuth.ts
│   ├── useKakaoMap.ts
│   └── useNotifications.ts
├── stores/
│   └── authStore.ts
├── types/
│   └── index.ts
├── lib/
│   ├── kakaoMap.ts            ← 카카오맵 SDK 초기화
│   └── utils.ts
├── styles/
│   └── global.css
├── App.tsx
└── main.tsx
```

### 2.3 인증 스토어

```typescript
type UserType = 'PARTICIPANT' | 'ORGANIZER' | 'CAFE_OWNER' | 'PLATFORM_ADMIN'

interface AuthUser {
  id: number
  userType: UserType
  hasProfile?: boolean    // PARTICIPANT만
  cafeId?: number         // CAFE_OWNER만
}

interface AuthState {
  accessToken: string | null
  user: AuthUser | null
  setAuth: (token: string, user: AuthUser) => void
  logout: () => void
}
```

토큰 갱신:
- Refresh token은 httpOnly 쿠키로 저장 (로그인 응답 시 Set-Cookie)
- `POST /api/auth/refresh`는 모든 사용자 유형이 공용으로 사용
- 서버가 refresh token의 JWT claims(userType)을 읽어 해당 유형에 맞는 access token을 재발급
- 401 응답 시 axios 인터셉터가 자동으로 refresh 호출
- refresh 실패 시 해당 역할의 로그인 페이지로 리다이렉트 (`/login`, `/organizer/login`, `/cafe-owner/login`, `/admin/login`)

### 2.4 라우팅

#### 참가자 (모바일 최적화)

```
/login                              ← 카카오 로그인
/auth/kakao/callback                ← OAuth 콜백
/profile/setup                      ← 프로필 작성 (최초 1회)

/cafes/{slug}                       ← 카페 메인 (정보 + 이벤트 목록)
/cafes/{slug}/events/{id}           ← 이벤트 상세 (카카오맵 경로 안내)
/cafes/{slug}/events/{id}/apply     ← 이벤트 신청
/cafes/{slug}/events/{id}/choose    ← 선택 페이지
/cafes/{slug}/events/{id}/result    ← 매칭 결과

/me                                 ← 마이페이지
/me/profile                         ← 프로필 수정
/me/applications                    ← 내 신청 내역
/me/notifications                   ← 알림 목록
```

#### 주관자 (데스크톱)

```
/organizer/login                    ← 이메일/비밀번호 로그인
/organizer/dashboard                ← 대시보드 (진행 중 이벤트, 통계)
/organizer/partnerships             ← 제휴 카페 목록 + 요청 현황
/organizer/events                   ← 이벤트 목록
/organizer/events/new               ← 이벤트 생성 (제휴 카페 선택 → 생성)
/organizer/events/{id}              ← 이벤트 상세/수정
/organizer/events/{id}/applications ← 신청자 관리 (승인/거절)
/organizer/events/{id}/stats        ← 이벤트 결과 통계
/organizer/commissions              ← 수수료 내역
/organizer/marketplace              ← 마켓플레이스 내 글 관리
/organizer/notifications            ← 알림
```

#### 카페 주인 (데스크톱)

```
/cafe-owner/login                   ← 이메일/비밀번호 로그인
/cafe-owner/dashboard               ← 대시보드 (예정 이벤트, 제휴 현황, 수수료 요약)
/cafe-owner/my-cafe                 ← 카페 정보 관리 (카카오맵 위치 등록/수정)
/cafe-owner/partnerships            ← 제휴 관리 (요청 수신/수락/거절/해지)
/cafe-owner/events                  ← 예정 이벤트 확인 (읽기 전용)
/cafe-owner/commissions             ← 수수료 내역
/cafe-owner/marketplace             ← 마켓플레이스 내 글 관리
/cafe-owner/notifications           ← 알림
```

#### 플랫폼 관리자 (데스크톱)

```
/admin/login                        ← 이메일/비밀번호 로그인
/admin/dashboard                    ← 대시보드 (전체 통계)
/admin/cafes                        ← 카페 등록/관리
/admin/organizers                   ← 주관자 계정 관리
/admin/partnerships                 ← 전체 제휴 현황
/admin/commissions                  ← 수수료 관리 (주관자/카페 탭)
```

#### 마켓플레이스 (공용)

```
/marketplace                        ← 글 목록 (장소 제공 / 장소 구함 탭)
/marketplace/new                    ← 글 작성
/marketplace/{id}                   ← 글 상세 (카카오맵 위치)
/marketplace/{id}/edit              ← 글 수정
```

#### 라우트 보호

- 각 역할별 `ProtectedRoute` 컴포넌트로 접근 제어
- 잘못된 userType → 해당 역할 로그인 페이지로 리다이렉트
- 참가자: `hasProfile === false` → `/profile/setup` 리다이렉트
- 마켓플레이스: ORGANIZER 또는 CAFE_OWNER만 접근 가능

### 2.5 디자인 시스템

#### 디자인 방향

따뜻한 & 캐주얼. 코랄/옐로우 그라데이션, 라운드 UI, 친근한 톤.

#### 컬러 팔레트

```
Primary:        #FF6B6B (코랄)
Primary Dark:   #FF5252
Secondary:      #FFE66D (옐로우)
Gradient:       linear-gradient(135deg, #FF6B6B, #FFE66D)

Background:     #FFF5F5 (참가자 모바일)
Surface:        #FFFFFF
Text Primary:   #1A1A1A
Text Secondary: #888888

Success:        #4CAF50
Warning:        #F59E0B
Error:          #EF4444
```

#### 참가자 UI (모바일)

- 그라데이션 헤더 + 라운드 카드 레이아웃
- 큰 터치 타겟 (44px 이상)
- 하단 네비게이션 바
- 카드 기반 이벤트 목록 (정원 현황 프로그레스 바)
- 375px 기준 모바일 퍼스트

#### 관리자/주관자/카페 주인 UI (데스크톱)

- shadcn/ui 기반 (Table, Form, Dialog, Card, Tabs)
- 사이드바 네비게이션
- 데이터 테이블 + 통계 카드
- 코랄 포인트, 전체적으로 화이트/그레이 톤
- 1024px+ 최적화

### 2.6 카카오맵 연동

#### 사용 API

| API | 용도 | 비고 |
|-----|------|------|
| 카카오맵 JavaScript SDK | 지도 렌더링, 마커 | 무료 |
| 카카오 로컬 API | 주소 검색, 좌표 ↔ 주소 변환 | 무료 |
| 카카오 모빌리티 API | 경로 탐색 | 별도 등록 필요, 사용량 기반 과금 |

카카오 모빌리티 API는 별도 앱 등록이 필요하다. 무료 할당량 초과 시 과금되므로, 초기에는 카카오맵 길찾기 URL 링크(`kakaomap://route?...`)로 대체하고 트래픽 증가 시 인앱 경로 표시로 전환할 수 있다.

#### 사용 위치

**카페 주인 — 카페 정보 등록/수정**
- 주소 검색 (카카오 로컬 API) → 지도에 마커 표시
- 마커 드래그로 미세 조정
- 확정된 좌표(latitude, longitude)와 주소를 서버에 저장

**마켓플레이스 — 글 상세**
- 카페 위치 지도 표시 (읽기 전용 마커)
- OFFER_SPACE 글에만 표시

**참가자 — 이벤트 상세**
- 카페 위치 마커 (고정, 도착지)
- 출발지: 현재 GPS 위치 (기본값), 수정 가능 (지도 선택 또는 주소 검색)
- 경로 표시: 카카오맵 길찾기 URL 또는 카카오 모빌리티 API

### 2.7 TypeScript 타입 정의 (주요 DTO)

```typescript
// === Enums ===
type UserType = 'PARTICIPANT' | 'ORGANIZER' | 'CAFE_OWNER' | 'PLATFORM_ADMIN'
type Gender = 'MALE' | 'FEMALE'
type DrinkingType = 'NONE' | 'SOMETIMES' | 'OFTEN'
type SmokingType = 'NONE' | 'SOMETIMES' | 'OFTEN'
type EventStatus = 'OPEN' | 'CLOSED' | 'COMPLETED'
type ApplicationStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED' | 'COMPLETED'
type CommissionStatus = 'PENDING' | 'INVOICED' | 'PAID'
type CommissionTargetType = 'ORGANIZER' | 'CAFE_OWNER'
type PartnershipStatus = 'PENDING' | 'ACTIVE' | 'TERMINATED'
type MarketplacePostType = 'OFFER_SPACE' | 'SEEK_SPACE'
type NotificationType = 'NEW_APPLICATION' | 'APPROVED' | 'REJECTED' | 'MATCH_RESULT'
  | 'EVENT_REMINDER' | 'CHOICE_REMINDER' | 'EVENT_COMPLETED' | 'COMMISSION_INVOICE'
  | 'PARTNERSHIP_REQUESTED' | 'PARTNERSHIP_ACCEPTED' | 'PARTNERSHIP_REJECTED'

// === Entities ===
interface Organizer {
  id: number
  name: string
  phoneNumber: string
  email: string
  description: string | null
  commissionRate: number
}

interface CafeOwner {
  id: number
  name: string
  phoneNumber: string
  email: string
  cafeId: number
}

interface Cafe {
  id: number
  name: string
  address: string
  description: string | null
  logoUrl: string | null
  coverImageUrl: string | null
  slug: string
  latitude: number | null
  longitude: number | null
  commissionRate: number
  isActive: boolean
}

interface Event {
  id: number
  cafeId: number
  organizerId: number
  title: string
  date: string          // LocalDate
  time: string          // LocalTime
  price: number
  maleCapacity: number
  femaleCapacity: number
  currentMaleCount: number
  currentFemaleCount: number
  description: string | null
  choiceDeadline: string | null
  matchNotificationTime: string | null
  minAge: number | null
  maxAge: number | null
  maxChoices: number
  status: EventStatus
}

interface ParticipantProfile {
  id: number
  name: string
  age: number
  gender: Gender
  job: string
  height: number
  mbti: string
  hobby: string | null
  drinking: DrinkingType
  smoking: SmokingType
  religion: string | null
  idealType: string | null
  introduction: string | null
  photoUrl: string | null
}

interface Application {
  id: number
  participantId: number
  eventId: number
  status: ApplicationStatus
  appliedAt: string
  reviewedAt: string | null
  rejectReason: string | null
}

interface Partnership {
  id: number
  cafeId: number
  cafeName: string
  organizerId: number
  organizerName: string
  status: PartnershipStatus
  requestedBy: 'CAFE_OWNER' | 'ORGANIZER'
  message: string | null
  createdAt: string
}

interface MarketplacePost {
  id: number
  type: MarketplacePostType
  authorType: 'CAFE_OWNER' | 'ORGANIZER'
  authorId: number
  authorName: string
  title: string
  description: string
  region: string
  capacity: number | null
  preferredDate: string | null
  imageUrls: string[]           // DB: 쉼표 구분 TEXT, API 응답 시 배열로 변환
  cafeLatitude: number | null
  cafeLongitude: number | null
  cafeAddress: string | null
  isActive: boolean
  createdAt: string
}

interface Commission {
  id: number
  cafeId: number
  cafeName: string
  eventId: number
  eventTitle: string
  targetType: CommissionTargetType
  targetId: number
  participantCount: number
  eventPrice: number
  commissionRate: number
  unitPrice: number
  totalAmount: number
  status: CommissionStatus
  invoicedAt: string | null
  paidAt: string | null
}

interface MatchResultResponse {
  matched: boolean
  partnerNickname: string | null
  notifiedAt: string | null
}

interface Notification {
  id: number
  type: NotificationType
  title: string
  message: string
  isRead: boolean
  createdAt: string
}

interface ParticipantInfo {
  number: number
  gender: Gender
  age: number
  job: string
  introduction: string | null
}
```

### 2.8 페이지네이션

백엔드가 커서 기반 페이지네이션(`?cursor={lastId}&size=20`)을 사용하므로:

| 페이지 | 방식 |
|--------|------|
| 참가자 — 이벤트 목록, 신청 내역, 알림 | 무한 스크롤 (모바일 UX) |
| 주관자/카페 주인 — 테이블 형태 데이터 | Load More 버튼 |
| 관리자 — 전체 목록 | Load More 버튼 |
| 마켓플레이스 — 글 목록 | 무한 스크롤 |

### 2.9 빈 상태 / 에러 처리

#### 빈 상태 (EmptyState 공통 컴포넌트)

| 페이지 | 빈 상태 메시지 |
|--------|---------------|
| 이벤트 목록 | "아직 예정된 이벤트가 없어요" |
| 내 신청 내역 | "신청한 이벤트가 없어요" |
| 알림 | "새로운 알림이 없어요" |
| 주관자 — 제휴 목록 | "아직 제휴한 카페가 없어요. 마켓플레이스에서 카페를 찾아보세요" |
| 카페 주인 — 제휴 목록 | "아직 제휴 요청이 없어요. 마켓플레이스에 글을 등록해보세요" |
| 마켓플레이스 | "등록된 글이 없어요" |

#### 에러 처리

- 네트워크 에러: 토스트 알림 + 재시도 버튼
- 이벤트 정원 초과: "정원이 마감되었습니다" 안내 + 목록으로 돌아가기
- 카카오맵 SDK 로드 실패: 지도 영역에 주소 텍스트 + "지도를 불러올 수 없습니다" 표시
- 제휴 없이 이벤트 생성 시도: "먼저 카페와 제휴를 맺어주세요" + 마켓플레이스 링크
- 토큰 만료: 자동 갱신 실패 시 로그인 페이지 리다이렉트

### 2.10 반응형 전략

| 역할 | 주 디바이스 | 기준 |
|------|-----------|------|
| 참가자 | 모바일 | 375px 모바일 퍼스트 |
| 주관자 | 데스크톱 | 1024px+ 최적화, 태블릿 지원 |
| 카페 주인 | 데스크톱 | 1024px+ 최적화, 태블릿 지원 |
| 관리자 | 데스크톱 | 1280px+ 최적화 |
| 마켓플레이스 | 양쪽 | 반응형 (모바일 + 데스크톱) |
