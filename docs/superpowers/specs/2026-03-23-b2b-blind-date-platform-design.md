# B2B Blind Date Platform - 설계 문서

## 개요

오프라인 소개팅 이벤트를 운영하는 바/카페에 매칭 시스템을 제공하는 B2B SaaS 플랫폼.
기존 단일 관리자 구조의 소개팅 매칭 앱을 B2B 멀티테넌트 구조로 재설계한다.

### 목표 고객
- 이미 소개팅 이벤트를 운영 중인 바/라운지 (운영 효율화)
- 소개팅 이벤트를 새로 시작하고 싶은 일반 카페/바 (턴키 솔루션)

### 수익 모델
- 건당 수수료: 이벤트 확정 참가자 1인당 수수료 부과
- 바별 수수료율 설정 가능 (기본 10%)
- Phase 1: 바가 직접 수금, 월말 수수료 청구
- Phase 2 (바 10곳+): 플랫폼 수금 → 정산 전환

### 핵심 원칙
- 바 사장님은 카카오톡 알림만으로 운영 가능해야 한다
- 참가자는 바별 URL로 진입하여 웹에서 신청/선택/결과 확인
- 모든 데이터는 Bar를 기준으로 격리
- 참가자 계정은 플랫폼 레벨로 관리 (프로필 재사용 목적). 단, 바 사장님에게는 자기 바에 신청한 참가자 프로필만 노출됨

---

## 아키텍처

### 시스템 구조

```
[참가자 웹] ──nginx──▶ [Spring Boot API] ──▶ [MySQL]
[바 사장님 웹]                  │
[플랫폼 관리자 웹]              ├──▶ [Redis] (알림 큐)
                               ├──▶ [카카오 OAuth]
                               └──▶ [카카오 채널 메시지 API]
```

### 멀티테넌트 모델

- 공유 DB + barId 컬럼 방식
- 참가자(Participant)는 바에 종속되지 않음 (플랫폼 레벨)
- Event, Application, Choice, MatchResult, Commission은 Bar에 종속

### 역할 구분

| 역할 | 설명 | 인터페이스 |
|------|------|-----------|
| PLATFORM_ADMIN | 플랫폼 운영자. 바 등록, 수수료 관리, 전체 통계 | 웹 관리자 페이지 |
| BAR_OWNER | 바 사장님. 이벤트 생성, 신청자 승인 | 카톡 알림 + 최소 웹 페이지 |
| PARTICIPANT | 참가자. 이벤트 신청, 선택, 결과 확인 | 웹 (바별 페이지) + 카톡 알림 |

### 기술 스택

| 구분 | 선택 | 이유 |
|------|------|------|
| Backend | Kotlin + Spring Boot 3.2 | 기존 경험 활용 |
| DB | MySQL 8.0 | 기존 유지, 멀티테넌트는 공유 DB + barId 컬럼 |
| Cache/Queue | Redis 7 | 알림 큐 용도 |
| Frontend | React 19 + TypeScript + Vite 8 | 기존 유지 |
| 알림 | 카카오톡 채널 메시지 API | 알림톡보다 진입장벽 낮음 |
| 인증 | JWT (참가자: 카카오 OAuth, 바 사장님/관리자: 이메일/비밀번호) |

---

## 도메인 모델

### 엔티티

```
Bar (업소)
 ├── name: String
 ├── address: String
 ├── description: String (TEXT)
 ├── logoUrl: String
 ├── coverImageUrl: String
 ├── slug: String (unique)          ← URL용 (예: /bars/gangnam-lounge)
 ├── commissionRate: Int (기본 10)  ← 바별 수수료율 (%)
 └── isActive: Boolean

BarOwner (사장님 계정)
 ├── bar: Bar (N:1)
 ├── name: String
 ├── phoneNumber: String
 ├── email: String (unique)
 ├── kakaoId: String               ← 카톡 알림 수신용
 └── password: String              ← 웹 관리용

Participant (참가자)
 ├── kakaoId: String (unique)
 ├── nickname: String
 ├── phoneNumber: String
 └── isProfileComplete: Boolean

ParticipantProfile (프로필)
 ├── participant: Participant (1:1)
 ├── name: String
 ├── age: Int
 ├── gender: Gender (MALE/FEMALE)
 ├── job: String
 ├── height: Int
 ├── mbti: String (4)
 ├── hobby: String (TEXT)
 ├── drinking: DrinkingType
 ├── smoking: SmokingType
 ├── religion: String
 ├── idealType: String (TEXT)
 ├── introduction: String (TEXT)
 └── photoUrl: String

Event (이벤트)
 ├── bar: Bar (N:1)
 ├── title: String
 ├── date: LocalDate
 ├── time: LocalTime
 ├── price: Int
 ├── maleCapacity: Int
 ├── femaleCapacity: Int
 ├── currentMaleCount: Int (default 0)
 ├── currentFemaleCount: Int (default 0)
 ├── description: String (TEXT)
 ├── choiceDeadline: LocalDateTime?
 ├── matchNotificationTime: LocalDateTime?
 ├── minAge: Int?
 ├── maxAge: Int?
 ├── maxChoices: Int (default 3)           ← 바가 설정
 ├── matchingMode: MatchingMode (default BIDIRECTIONAL)
 ├── status: EventStatus (OPEN/CLOSED/COMPLETED)
 └── deletedAt: LocalDateTime?

 정원 카운트 규칙:
  - currentMaleCount/currentFemaleCount는 APPROVED 시 증가
  - CANCELLED 또는 APPROVED 이후 REJECTED 시 감소
  - PENDING/REJECTED(결제전) 상태 변경은 카운트에 영향 없음

Application (신청)
 ├── participant: Participant (N:1)
 ├── event: Event (N:1)
 ├── status: ApplicationStatus (PENDING/APPROVED/REJECTED/CANCELLED/COMPLETED)
 ├── appliedAt: LocalDateTime
 ├── reviewedAt: LocalDateTime?
 ├── reviewedBy: BarOwner? (N:1)
 └── rejectReason: String? (TEXT)

ParticipantNumber (참가번호)
 ├── event: Event (N:1)
 ├── participant: Participant (N:1)
 ├── number: Int
 └── gender: Gender

Choice (선택)
 ├── event: Event (N:1)
 ├── chooser: Participant (N:1)
 ├── chosen: Participant (N:1)
 └── createdAt: LocalDateTime

MatchResult (매칭 결과)
 ├── event: Event (N:1)
 ├── member1: Participant (N:1)    ← 남성
 ├── member2: Participant (N:1)    ← 여성
 ├── notified: Boolean (default false)
 └── notifiedAt: LocalDateTime?

Commission (수수료)
 ├── bar: Bar (N:1)
 ├── event: Event (N:1)
 ├── participantCount: Int         ← APPROVED 이상 상태의 참가자 수
 ├── eventPrice: Int               ← 이벤트 참가비 (스냅샷)
 ├── commissionRate: Int           ← 적용 수수료율 (스냅샷)
 ├── unitPrice: Int                ← eventPrice * commissionRate / 100
 ├── totalAmount: Int              ← participantCount × unitPrice
 ├── status: CommissionStatus (PENDING/INVOICED/PAID)
 ├── invoicedAt: LocalDateTime?
 └── paidAt: LocalDateTime?

ActionToken (카톡 액션용 일회용 토큰)
 ├── token: String (unique)        ← UUID v4 (36자), 단일 사용 후 즉시 무효화 (atomic CAS)
 ├── actionType: String            ← APPROVE_APPLICATION, REJECT_APPLICATION 등
 ├── targetId: Long                ← 대상 엔티티 ID
 ├── barOwnerId: Long
 ├── used: Boolean (default false)
 └── expiresAt: LocalDateTime      ← 24시간 만료
 보안: /api/actions/ 엔드포인트에 IP 기반 rate limiting 적용 (분당 10회)
       응답 페이지에 신청자 PII 미노출 (이름 마스킹: 홍*동)

Notification (알림)
 ├── recipientType: RecipientType (PARTICIPANT/BAR_OWNER)
 ├── recipientId: Long             ← participant.id 또는 barOwner.id
 ├── type: NotificationType
 ├── title: String
 ├── message: String (TEXT)
 └── isRead: Boolean (default false)

PlatformAdmin (플랫폼 관리자)
 ├── email: String (unique)
 ├── password: String
 └── name: String
```

### Enum 정의

```
Gender: MALE, FEMALE
DrinkingType: NONE, SOMETIMES, OFTEN
SmokingType: NONE, SOMETIMES, OFTEN
EventStatus: OPEN, CLOSED, COMPLETED
ApplicationStatus: PENDING, APPROVED, REJECTED, CANCELLED, COMPLETED
MatchingMode: BIDIRECTIONAL
 ※ BIDIRECTIONAL만 MVP에 포함. 향후 추가 모드는 Phase 2에서 검토
CommissionStatus: PENDING, INVOICED, PAID
NotificationType: NEW_APPLICATION, APPROVED, REJECTED, MATCH_RESULT,
                  EVENT_REMINDER, CHOICE_REMINDER, EVENT_COMPLETED, COMMISSION_INVOICE
```

### 기존 대비 변경 요약

| 기존 | 변경 | 이유 |
|------|------|------|
| Member + Role(USER/ADMIN) | Participant, BarOwner, PlatformAdmin 분리 | 역할별 필드/인증이 완전히 다름 |
| Payment | 제거 (Phase 1) | 바가 직접 수금 |
| BarVisitLog | 제거 | 체크인/체크아웃 불필요 |
| BarReservation | 제거 | 서비스 범위 밖 |
| 신규 Commission | 추가 | B2B 수수료 추적 핵심 |
| 신규 ActionToken | 추가 | 카톡 버튼 액션 처리 |
| Event에 bar, maxChoices, matchingMode | 추가 | 멀티테넌트 + 커스터마이징 |

---

## 이벤트 상태 머신

```
OPEN ──(choiceDeadline 도달, 스케줄러)──▶ CLOSED
OPEN ──(바 사장님 수동 마감)──────────▶ CLOSED
CLOSED ──(매칭 처리 + 알림 완료, 스케줄러)──▶ COMPLETED
```

- OPEN → CLOSED: `choiceDeadline` 도달 시 스케줄러가 자동 전환. 바 사장님이 수동으로 조기 마감도 가능
- CLOSED → COMPLETED: 매칭 처리 + 결과 알림 발송 완료 후 자동 전환
- 역방향 전환 불가 (CLOSED → OPEN 불가)

### 참가번호 부여 시점

- 이벤트 상태가 CLOSED로 전환될 때 스케줄러가 자동 부여
- 대상: APPROVED 상태인 모든 참가자
- 남녀 각각 1번부터 순차 번호 부여

### 참가자 선택 시 표시 정보

선택 페이지(`GET /api/events/{id}/participants`)에서 이성 참가자에게 보이는 정보:
- 참가번호, 나이, 직업, 한줄 자기소개
- 이름/사진/연락처는 비공개 (매칭 성사 후 닉네임만 공개)

## JWT 인증 구조

3개 사용자 유형을 단일 JWT 체계로 처리한다.

JWT claims:
```json
{
  "sub": "123",
  "userType": "PARTICIPANT | BAR_OWNER | PLATFORM_ADMIN",
  "barId": 456  // BAR_OWNER일 때만 포함
}
```

- `JwtAuthenticationFilter`에서 `userType`에 따라 `SecurityContext`에 적절한 principal 설정
- Security config에서 경로별 `userType` 검증:
  - `/api/bar-owner/**` → BAR_OWNER만
  - `/api/admin/**` → PLATFORM_ADMIN만
  - `/api/me/**`, `/api/events/**` 등 → PARTICIPANT만
  - `/api/bars/**` (GET), `/api/actions/**` → 인증 불필요

---

## 핵심 비즈니스 플로우

### 1. 바 온보딩

```
바 사장님과 미팅/연락
 → 플랫폼 관리자가 Bar + BarOwner 등록
 → 바 사장님에게 카톡으로 관리 페이지 링크 + 로그인 정보 전달
 → 바 사장님이 첫 이벤트 생성 (또는 관리자가 대신 생성)
```

초기에 바가 소수이므로 셀프 가입은 불필요. 직접 등록이 오히려 신뢰감을 줌.

### 2. 이벤트 생성 ~ 신청

```
바 사장님: 웹에서 이벤트 생성
 (날짜, 시간, 정원, 참가비, 선택 인원, 매칭 방식)
 → 이벤트 페이지 자동 생성: /bars/{slug}/events/{id}
 → 바 사장님이 이 링크를 SNS에 공유하여 모객

참가자: 링크 클릭 → 카카오 로그인 → 프로필 작성(최초 1회) → 신청
 → 바 사장님에게 카톡 알림: "새 신청이 있습니다 [승인/거절]"
 → 바 사장님: 카톡에서 승인 버튼 클릭
 → 참가자에게 카톡 알림: "승인되었습니다!"
```

### 3. 이벤트 당일 (매칭)

```
이벤트 시작 → 참가자에게 참가번호 자동 부여
 → 참가자: 웹에서 이성 참가자 번호 확인 → 선택 제출 (maxChoices 이내, choiceDeadline 전)
 → 선택 마감 → 매칭 자동 처리 (matchingMode에 따라)
 → 매칭 결과 → 참가자에게 카톡 알림 (성공/실패 모두)
 → 바 사장님에게 카톡: "이벤트 완료. 참가자 16명, 매칭 5쌍"
```

### 4. 정산

```
이벤트 COMPLETED 시 Commission 자동 생성
 → 월말: 플랫폼 관리자가 바별 수수료 합산 → 청구서 발행 (PENDING → INVOICED)
 → 바에서 수수료 입금 확인 → PAID 처리
```

Phase 1에서는 청구/입금 확인 수동 처리.

---

## API 설계

### 참가자용 (카카오 로그인 필요)

```
POST   /api/auth/kakao                    ← 카카오 로그인/회원가입
POST   /api/auth/refresh                   ← 토큰 갱신

GET    /api/me                             ← 내 정보
POST   /api/me/profile                     ← 프로필 생성
PUT    /api/me/profile                     ← 프로필 수정

GET    /api/bars/{slug}                    ← 바 정보 (공개)
GET    /api/bars/{slug}/events             ← 바의 이벤트 목록 (공개)
GET    /api/bars/{slug}/events/{id}        ← 이벤트 상세 (공개)

POST   /api/events/{id}/apply              ← 이벤트 신청
DELETE /api/events/{id}/apply              ← 신청 취소
GET    /api/me/applications                ← 내 신청 내역

GET    /api/events/{id}/participants       ← 이성 참가자 번호 목록
POST   /api/events/{id}/choices            ← 선택 제출
PUT    /api/events/{id}/choices            ← 선택 변경
GET    /api/events/{id}/result             ← 매칭 결과

GET    /api/me/notifications               ← 알림 목록
PUT    /api/notifications/{id}/read        ← 읽음 처리
```

### 바 사장님용 (이메일/비밀번호 로그인)

```
POST   /api/bar-owner/auth/login           ← 로그인

GET    /api/bar-owner/my-bar               ← 내 바 정보
PUT    /api/bar-owner/my-bar               ← 바 정보 수정

POST   /api/bar-owner/events               ← 이벤트 생성
PUT    /api/bar-owner/events/{id}          ← 이벤트 수정
DELETE /api/bar-owner/events/{id}          ← 이벤트 삭제
GET    /api/bar-owner/events               ← 내 이벤트 목록

GET    /api/bar-owner/events/{id}/applications  ← 신청자 목록
PUT    /api/bar-owner/applications/{id}/approve  ← 승인
PUT    /api/bar-owner/applications/{id}/reject   ← 거절

GET    /api/bar-owner/events/{id}/stats    ← 이벤트 결과 통계
GET    /api/bar-owner/commissions          ← 수수료 내역
```

### 카톡 알림 액션용

```
GET    /api/actions/{token}                ← 액션 정보 조회
POST   /api/actions/{token}/execute        ← 액션 실행 (승인/거절)
```

일회용 토큰 기반. 로그인 없이 동작. 24시간 만료.

### 플랫폼 관리자용

```
POST   /api/admin/auth/login
POST   /api/admin/bars                     ← 바 등록
GET    /api/admin/bars                     ← 바 목록
POST   /api/admin/bar-owners               ← 사장님 계정 생성
GET    /api/admin/commissions              ← 전체 수수료 현황
PUT    /api/admin/commissions/{id}/invoice  ← 청구
PUT    /api/admin/commissions/{id}/paid     ← 입금 확인
GET    /api/admin/dashboard                ← 통계 대시보드
```

---

## 알림 시스템

### 발송 채널
- 앱 내 Notification 저장 (DB)
- 카카오톡 채널 메시지 API (채널 친구 추가 필요)

채널 메시지를 선택한 이유: 알림톡 대비 템플릿 심사 없음, 1,000건/월 무료, 초기 진입장벽 낮음.

### 알림 목록

| 수신자 | 시점 | 메시지 | 액션 |
|--------|------|--------|------|
| 바 사장님 | 새 신청 | "홍길동(28/남) 님이 금요일 이벤트에 신청했습니다" | [승인] [거절] [프로필 보기] |
| 바 사장님 | 이벤트 완료 | "이벤트 완료! 참가자 16명, 매칭 5쌍" | [상세 보기] |
| 바 사장님 | 수수료 청구 | "3월 수수료 12만원이 청구되었습니다" | [내역 보기] |
| 참가자 | 신청 승인 | "XX라운지 금요일 이벤트 참가가 확정되었습니다!" | [이벤트 보기] |
| 참가자 | 신청 거절 | "아쉽지만 이번 이벤트 참가가 어렵습니다" | - |
| 참가자 | 선택 시간 안내 | "곧 선택 시간입니다! 마감: 오후 9시" | [선택하기] |
| 참가자 | 매칭 성공 | "축하합니다! 매칭 결과를 확인하세요" | [결과 보기] |
| 참가자 | 매칭 실패 | "아쉽지만 이번에는 매칭되지 않았습니다" | - |

### 바 사장님 카톡 액션 동작

```
카톡 메시지의 [승인] 버튼
 → /api/actions/{일회용토큰}/execute 호출
 → 로그인 없이 해당 신청 승인 처리
 → "승인 완료!" 응답 페이지 표시
```

---

## 프론트엔드 구조

### 화면 영역 (3개)

```
참가자 화면 (/bars/{slug}/...)
 ├── 바 메인 (바 소개 + 이벤트 목록)
 ├── 이벤트 상세 + 신청
 ├── 프로필 설정
 ├── 선택 페이지
 ├── 매칭 결과
 └── 마이페이지 (내 신청 내역, 알림)

바 사장님 화면 (/owner/...)
 ├── 로그인
 ├── 대시보드 (오늘 이벤트, 최근 신청)
 ├── 이벤트 관리 (생성/수정)
 ├── 신청자 관리 (프로필 보고 승인/거절)
 └── 수수료 내역

플랫폼 관리자 화면 (/admin/...)
 ├── 로그인
 ├── 바 관리 (등록/목록)
 ├── 수수료 관리 (청구/입금 확인)
 └── 대시보드 (전체 통계)
```

참가자는 바별 URL(/bars/gangnam-lounge)로 진입. 바 사장님이 이 링크를 SNS에 공유하여 모객. 바의 로고/커버이미지로 브랜딩.

---

## MVP 범위

### Phase 1 (MVP) - 포함

- 바/사장님 등록 (플랫폼 관리자 수동)
- 이벤트 CRUD (바 사장님 웹)
- 바별 이벤트 페이지 (참가자 모객용)
- 카카오 로그인 + 프로필
- 신청 + 승인/거절 (카톡 알림 + 액션 버튼)
- 매칭 (선택 인원수/매칭 방식 바별 설정)
- 매칭 결과 알림 (성공/실패 모두)
- 수수료 추적 (이벤트 완료 시 자동 생성, 수동 청구/확인)
- 플랫폼 대시보드

### Phase 2 이후

- 토스페이먼츠 결제 통합 + 자동 정산 (바 10곳+)
- 바 사장님 셀프 가입 (바 20곳+)
- 참가자 후기/평점
- 반복 이벤트 (매주 자동 생성)
- 참가자 간 채팅
- 추가 매칭 모드 (UNIDIRECTIONAL 등)

---

## 보완 사항

### BarOwner-Bar 관계
- 1:1 관계. BarOwner는 하나의 Bar만 관리 (unique 제약)
- 한 Bar에 여러 BarOwner는 Phase 2에서 검토

### 알림 발송 한도
- 카카오 채널 메시지 무료 1,000건/월. 초과 시 건당 15원
- 초기 바 5곳 × 이벤트 4회/월 × 참가자 20명 = 약 400건 + 관리자 알림 → 무료 범위 내
- 무료 한도 초과 시 유료 전환 (월 비용을 수수료 수익으로 커버)

### 파일 업로드
- 프로필 사진, 바 로고/커버: 프리사인드 URL 방식 S3 업로드
- `POST /api/upload/presigned-url` → S3 presigned URL 반환 → 클라이언트가 직접 S3에 업로드
- 제한: 이미지만, 최대 5MB

### 페이지네이션
- 목록 API는 커서 기반 페이지네이션 적용
- 쿼리 파라미터: `?cursor={lastId}&size=20`
