# 블라인드 소개팅 앱 설계 문서

## 1. 개요

그룹 소개팅 매칭 + 혼술바 운영 웹앱. 관리자가 소개팅 이벤트를 생성하고, 사용자가 신청/결제하면 관리자가 승인/거절하는 구조. 소개팅 종료 후 참가자들이 마음에 드는 이성을 선택하고, 양방향 매칭 시 알림을 발송한다. 혼술바는 고정 장소에서 운영되며, 실시간 남/여 인원 및 잔여석을 앱에서 확인할 수 있고, 예약(자동 확정) 및 워크인(관리자 수동 관리)을 지원한다.

### 기술 스택

- **Frontend**: React (모바일 최적화 웹앱, PWA)
- **Backend**: Spring Boot + Kotlin
- **Database**: MySQL
- **Cache/Queue**: Redis
- **외부 연동**: 카카오 OAuth2, 카카오 알림톡, 토스페이먼츠

## 2. 아키텍처

모듈형 모놀리스 구조. 하나의 Spring Boot 애플리케이션 내에서 도메인별 모듈로 분리.

```
com.blinddate/
├── member/         # 회원 도메인 (entity, controller, service, repository)
├── event/          # 소개팅 이벤트 도메인
├── application/    # 신청 도메인
├── payment/        # 결제 도메인
├── matching/       # 번호 부여, 선택, 매칭 도메인
├── notification/   # 알림 도메인
├── bar/            # 혼술바 도메인
├── auth/           # 인증/인가
└── common/         # 공통 모듈
```

### 외부 시스템 연동

| 시스템 | 용도 |
|--------|------|
| Kakao OAuth2 | 소셜 로그인 |
| Kakao 알림톡 | 승인/거절/매칭 결과 알림 |
| 토스페이먼츠 | 결제/환불 처리 |
| 외부 웹훅 (Slack 등) | 관리자 신규 신청 알림 |

## 3. 전체 플로우

1. 카카오 로그인 → 최초 시 프로필 등록
2. 캘린더에서 이벤트 확인 → 이벤트 상세에서 신청
3. 토스페이먼츠 결제 → 결제 완료 시 신청 접수
4. 관리자에게 알림 (앱 내 + 외부 웹훅)
5. 관리자 **승인** → 참가자 번호 자동 부여 (남/여 별도) + 카카오 알림톡
6. 관리자 **거절** → 토스페이먼츠 자동 환불 + 카카오 알림톡
7. 소개팅 당일 진행
8. 소개팅 종료 후 → 앱에서 마음에 드는 이성 번호 최대 3명 선택
9. 선택 마감 → 양방향 매칭 확인 → 매칭된 쌍에게 카카오 알림톡 발송

### 혼술바 플로우

1. 앱에서 혼술바 탭 → 실시간 현재 남/여 인원 + 남은 자리 확인
2. 자리가 있으면 예약 신청 → 자동 확정 (결제 없음, 현장 결제)
3. 워크인 손님은 관리자가 직접 입장/퇴장 처리
4. 인원 변동 시 Redis를 통해 실시간 반영

### 신청 상태 흐름

```
PAYMENT_WAITING → PAID → APPROVED → COMPLETED
                       → REJECTED (자동 환불)
사용자 취소: PAID → CANCELLED (자동 환불)
```

## 4. 데이터 모델

### Member (회원)

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT PK | |
| kakao_id | VARCHAR UNIQUE | 카카오 고유 ID |
| email | VARCHAR | 카카오 이메일 |
| nickname | VARCHAR | 닉네임 |
| phone_number | VARCHAR | 휴대폰 번호 (카카오 알림톡용) |
| role | ENUM(USER, ADMIN) | 역할 |
| created_at | DATETIME | |
| updated_at | DATETIME | |

### MemberProfile (프로필)

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT PK | |
| member_id | BIGINT FK → Member | |
| name | VARCHAR | 이름 |
| age | INT | 나이 |
| gender | ENUM(MALE, FEMALE) | 성별 |
| job | VARCHAR | 직업 |
| height | INT | 키 |
| mbti | VARCHAR(4) | MBTI |
| hobby | TEXT | 취미 |
| drinking | ENUM(NONE, SOMETIMES, OFTEN) | 음주 |
| smoking | ENUM(NONE, SOMETIMES, OFTEN) | 흡연 |
| religion | VARCHAR | 종교 |
| ideal_type | TEXT | 이상형 |
| introduction | TEXT | 자기소개 |
| photo_url | VARCHAR | 프로필 사진 URL |
| created_at | DATETIME | |
| updated_at | DATETIME | |

### BlindDateEvent (소개팅 이벤트)

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT PK | |
| title | VARCHAR | 이벤트 제목 |
| date | DATE | 날짜 |
| time | TIME | 시간 |
| male_capacity | INT | 남자 정원 |
| female_capacity | INT | 여자 정원 |
| current_male_count | INT | 현재 승인된 남자 수 |
| current_female_count | INT | 현재 승인된 여자 수 |
| price | INT | 참가 금액 (원) |
| status | ENUM(OPEN, CLOSED, COMPLETED) | 이벤트 상태 |
| description | TEXT | 설명 |
| choice_deadline | DATETIME | 이성 선택 마감 시각 |
| match_notification_time | DATETIME | 매칭 결과 알림 발송 시각 |
| min_age | INT (nullable) | 참가 최소 나이 |
| max_age | INT (nullable) | 참가 최대 나이 |
| created_by | BIGINT FK → Member | 생성한 관리자 |
| created_at | DATETIME | |
| updated_at | DATETIME | |
| deleted_at | DATETIME (nullable) | 소프트 삭제 |

### Application (신청)

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT PK | |
| member_id | BIGINT FK → Member | 신청자 |
| event_id | BIGINT FK → BlindDateEvent | 이벤트 |
| status | ENUM(PAYMENT_WAITING, PAID, APPROVED, REJECTED, CANCELLED, COMPLETED) | 상태 |
| applied_at | DATETIME | 신청 시각 |
| reviewed_at | DATETIME | 심사 시각 |
| reviewed_by | BIGINT FK → Member | 심사한 관리자 |
| reject_reason | TEXT (nullable) | 거절 사유 |

UNIQUE 제약: (member_id, event_id) — 동일 이벤트에 중복 신청 방지

### Payment (결제)

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT PK | |
| application_id | BIGINT FK → Application | 연결된 신청 |
| member_id | BIGINT FK → Member | 결제자 |
| amount | INT | 결제 금액 |
| payment_key | VARCHAR | 토스페이먼츠 결제 키 |
| order_id | VARCHAR UNIQUE | 주문 ID |
| status | ENUM(PENDING, PAID, REFUNDED, FAILED) | 결제 상태 |
| paid_at | DATETIME | 결제 완료 시각 |
| refunded_at | DATETIME (nullable) | 환불 시각 |
| created_at | DATETIME | |

### ParticipantNumber (참가자 번호)

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT PK | |
| event_id | BIGINT FK → BlindDateEvent | 이벤트 |
| member_id | BIGINT FK → Member | 참가자 |
| number | INT | 부여된 번호 |
| gender | ENUM(MALE, FEMALE) | 성별 (남/여 별도 번호) |

UNIQUE 제약: (event_id, gender, number)

### Choice (선택)

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT PK | |
| event_id | BIGINT FK → BlindDateEvent | 이벤트 |
| chooser_id | BIGINT FK → Member | 선택한 사람 |
| chosen_id | BIGINT FK → Member | 선택된 사람 |
| created_at | DATETIME | |

제약: 1인당 이벤트당 최대 3명 선택, 이성만 선택 가능

### MatchResult (매칭 결과)

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT PK | |
| event_id | BIGINT FK → BlindDateEvent | 이벤트 |
| member1_id | BIGINT FK → Member | 매칭된 사람 1 |
| member2_id | BIGINT FK → Member | 매칭된 사람 2 |
| notified | BOOLEAN | 알림 발송 여부 |
| notified_at | DATETIME (nullable) | 알림 발송 시각 |
| created_at | DATETIME | |

UNIQUE 제약: (event_id, member1_id, member2_id), member1_id는 항상 MALE

### Notification (알림)

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT PK | |
| member_id | BIGINT FK → Member | 수신자 |
| type | ENUM(NEW_APPLICATION, APPROVED, REJECTED, MATCH_RESULT, EVENT_REMINDER) | 타입 |
| title | VARCHAR | 제목 |
| message | TEXT | 내용 |
| is_read | BOOLEAN | 읽음 여부 |
| created_at | DATETIME | |

### Bar (혼술바)

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT PK | |
| name | VARCHAR | 가게 이름 |
| address | VARCHAR | 주소 |
| total_seats | INT | 총 좌석 수 |
| current_male_count | INT | 현재 남자 인원 |
| current_female_count | INT | 현재 여자 인원 |
| is_open | BOOLEAN | 현재 영업 중 여부 |
| open_time | TIME | 영업 시작 시간 |
| close_time | TIME | 영업 종료 시간 |
| created_at | DATETIME | |
| updated_at | DATETIME | |

### BarReservation (혼술바 예약)

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT PK | |
| bar_id | BIGINT FK → Bar | 혼술바 |
| member_id | BIGINT FK → Member | 예약자 |
| date | DATE | 예약 날짜 |
| time | TIME | 예약 시간 |
| status | ENUM(CONFIRMED, CANCELLED, VISITED, NO_SHOW) | 예약 상태 |
| created_at | DATETIME | |

### BarVisitLog (혼술바 입퇴장 기록)

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT PK | |
| bar_id | BIGINT FK → Bar | 혼술바 |
| member_id | BIGINT FK → Member (nullable) | 회원 (워크인은 null) |
| reservation_id | BIGINT FK → BarReservation (nullable) | 예약 연결 (워크인은 null) |
| gender | ENUM(MALE, FEMALE) | 성별 |
| check_in_at | DATETIME | 입장 시각 |
| check_out_at | DATETIME (nullable) | 퇴장 시각 |

### 관계 요약

- Member 1:1 MemberProfile
- Member 1:N Application
- BlindDateEvent 1:N Application
- Application 1:1 Payment
- BlindDateEvent 1:N ParticipantNumber
- BlindDateEvent 1:N Choice
- BlindDateEvent 1:N MatchResult
- Member 1:N Notification
- Bar 1:N BarReservation
- Bar 1:N BarVisitLog
- Member 1:N BarReservation

## 5. API 설계

### Auth (인증)

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | /api/auth/kakao/login | 카카오 로그인 (인가코드 → JWT) |
| POST | /api/auth/refresh | Access Token 재발급 |
| DELETE | /api/auth/logout | 로그아웃 (Redis 토큰 삭제) |

### Member (회원/프로필)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /api/members/me | 내 회원 정보 |
| POST | /api/members/me/profile | 프로필 최초 등록 |
| PUT | /api/members/me/profile | 프로필 수정 |
| GET | /api/members/me/profile | 내 프로필 조회 |
| POST | /api/members/me/photo | 사진 업로드 |

### Event (소개팅 이벤트)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /api/events | 이벤트 목록 (월별, 캘린더용) |
| GET | /api/events/{id} | 이벤트 상세 |
| POST | /api/admin/events | [관리자] 이벤트 생성 |
| PUT | /api/admin/events/{id} | [관리자] 이벤트 수정 |
| DELETE | /api/admin/events/{id} | [관리자] 이벤트 삭제 |

### Application (신청)

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | /api/events/{id}/apply | 신청 (→ 결제 대기 상태) |
| DELETE | /api/applications/{id} | 신청 취소 (자동 환불) |
| GET | /api/members/me/applications | 내 신청 내역 |
| GET | /api/admin/applications | [관리자] 전체 신청 목록 |
| GET | /api/admin/applications/{id} | [관리자] 신청 상세 (프로필 포함) |
| PUT | /api/admin/applications/{id}/approve | [관리자] 승인 |
| PUT | /api/admin/applications/{id}/reject | [관리자] 거절 (자동 환불) |

### Payment (결제)

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | /api/payments/confirm | 토스페이먼츠 결제 승인 (콜백) |
| POST | /api/payments/webhook | 토스페이먼츠 서버 웹훅 (서명 검증) |
| GET | /api/payments/{applicationId} | 결제 정보 조회 |

### Matching (선택/매칭)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /api/events/{id}/participants | 내 이벤트 참가자 번호 목록 (이성만) |
| POST | /api/events/{id}/choices | 마음에 드는 번호 선택 (최대 3명) |
| GET | /api/events/{id}/match-result | 내 매칭 결과 조회 |

### Bar (혼술바)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /api/bar/status | 혼술바 실시간 현황 (남/여 인원, 잔여석, 영업 여부) |
| POST | /api/bar/reservations | 혼술바 예약 (자동 확정) |
| DELETE | /api/bar/reservations/{id} | 예약 취소 |
| GET | /api/members/me/bar-reservations | 내 혼술바 예약 내역 |
| PUT | /api/admin/bar/open | [관리자] 영업 시작 |
| PUT | /api/admin/bar/close | [관리자] 영업 종료 |
| POST | /api/admin/bar/check-in | [관리자] 입장 처리 (성별 필수, 회원 선택) |
| POST | /api/admin/bar/check-out/{visitLogId} | [관리자] 퇴장 처리 |
| GET | /api/admin/bar/visitors | [관리자] 현재 방문자 목록 |

### Notification (알림)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /api/notifications | 내 알림 목록 |
| PUT | /api/notifications/{id}/read | 읽음 처리 |
| GET | /api/notifications/unread-count | 읽지 않은 알림 수 |

## 6. Redis 활용

| 용도 | Key 패턴 | TTL | 설명 |
|------|----------|-----|------|
| Refresh Token | `auth:refresh:{memberId}` | 14일 | JWT 리프레시 토큰 |
| Token 블랙리스트 | `auth:blacklist:{accessToken}` | Access Token 잔여 시간 | 로그아웃된 토큰 |
| 이벤트 목록 캐시 | `event:list:{yearMonth}` | 1시간 | 월별 이벤트 목록 |
| 이벤트 잔여석 | `event:seats:{eventId}` | 5분 | 잔여석 현황 |
| 알림 큐 | `notification:queue` | - | Redis List, 알림 발송 큐 |
| 혼술바 현황 | `bar:status:{barId}` | - (실시간 갱신) | 현재 남/여 인원, 잔여석 (Hash) |

## 7. 프론트엔드 라우팅

| 경로 | 화면 | 비고 |
|------|------|------|
| /login | 카카오 로그인 | |
| /profile/setup | 최초 프로필 등록 | |
| / | 캘린더 (메인) | |
| /events/:id | 이벤트 상세 | |
| /events/:id/choose | 이성 선택 화면 | 소개팅 종료 후 |
| /events/:id/result | 매칭 결과 | |
| /mypage | 마이페이지 | |
| /mypage/applications | 내 신청 내역 | |
| /mypage/profile | 프로필 수정 | |
| /bar | 혼술바 실시간 현황 | |
| /bar/reserve | 혼술바 예약 | |
| /notifications | 알림 목록 | |
| /admin/events | [관리자] 이벤트 관리 | |
| /admin/applications | [관리자] 신청 관리 | |
| /admin/members | [관리자] 회원 관리 | |
| /admin/bar | [관리자] 혼술바 관리 (입퇴장) | |

### 하단 네비게이션

- 캘린더 (소개팅)
- 혼술바
- 내 신청
- 알림 (안읽은 수 배지)
- 마이
- 관리 (ADMIN일 때만 노출)

## 8. UI 디자인 가이드

- **테마 컬러**: 핑크-오렌지 그라데이션 (#ff6b8a → #ff8e53)
- **관리자 테마**: 보라색 (#6366f1 → #8b5cf6)
- **모바일 최적화**: 375px 기준, 반응형
- **폰트**: Noto Sans KR
- **카드 기반 UI**: 둥근 모서리 (border-radius: 14-16px), 부드러운 그림자
- **하단 네비게이션**: 고정, 아이콘 + 텍스트

## 9. 알림 시나리오

| 이벤트 | 수신자 | 채널 | 내용 |
|--------|--------|------|------|
| 새 신청 접수 (결제 완료) | 관리자 | 앱 내 + 웹훅 | "새로운 신청이 접수되었습니다" |
| 승인 | 신청자 | 앱 내 + 카카오 알림톡 | "신청이 승인되었습니다. 번호: N번" |
| 거절 (환불) | 신청자 | 앱 내 + 카카오 알림톡 | "신청이 거절되었습니다. 환불이 진행됩니다" |
| 양방향 매칭 | 매칭된 양측 | 앱 내 + 카카오 알림톡 | "매칭이 되었습니다! 💕" |
| 사용자 취소 (환불) | 신청자 | 앱 내 | "신청이 취소되었습니다. 환불이 진행됩니다" |
| 이벤트 리마인더 | 승인된 참가자 | 앱 내 + 카카오 알림톡 | "내일 소개팅이 예정되어 있습니다!" (D-1) |

## 10. 파일 저장소

- **저장소**: AWS S3 (또는 MinIO)
- **허용 포맷**: JPG, PNG, WEBP
- **최대 파일 크기**: 10MB
- **URL 방식**: S3 presigned URL 또는 CDN (CloudFront) 연동
- **경로 규칙**: `profiles/{memberId}/{uuid}.{ext}`

## 11. 인증 상세

| 항목 | 설정 |
|------|------|
| Access Token TTL | 30분 |
| Refresh Token TTL | 14일 |
| 서명 알고리즘 | HS256 |
| 클라이언트 저장 | Access Token: 메모리, Refresh Token: httpOnly Cookie |
| Refresh Token 회전 | 재발급 시 기존 토큰 폐기 + 새 토큰 발급 |

## 12. 동시성 처리

- **잔여석 관리**: `current_male_count`, `current_female_count`는 승인 시 `SELECT ... FOR UPDATE` (비관적 락)으로 갱신하여 동시 승인 시 정합성 보장
- **선택 제한**: Choice 저장 시 트랜잭션 내에서 기존 선택 수를 확인하여 3명 초과 방지
- **혼술바 인원**: Redis Hash (`bar:status:{barId}`)에서 `HINCRBY`로 원자적 증감, 입퇴장 시 DB와 동기화

## 13. 스케줄러

| 작업 | 트리거 | 설명 |
|------|--------|------|
| 매칭 처리 | `choice_deadline` 도달 시 | 양방향 선택을 확인하여 MatchResult 생성 |
| 매칭 알림 발송 | `match_notification_time` 도달 시 | 매칭된 쌍에게 카카오 알림톡 발송 |
| 이벤트 리마인더 | 이벤트 D-1 오전 10시 | 승인된 참가자에게 리마인더 발송 |
| 이벤트 상태 전환 | 이벤트 날짜 경과 후 | APPROVED → COMPLETED, 이벤트 OPEN → COMPLETED |

## 14. 알림 큐 아키텍처

- **큐**: Redis List (`notification:queue`)에 JSON 메시지 push
- **컨슈머**: Spring `@Scheduled` 폴링 (1초 간격), `BRPOP` 사용
- **동시성**: 단일 컨슈머 (현재 규모에 충분)
- **재시도**: 카카오 알림톡 실패 시 최대 3회 재시도 (지수 백오프: 1초, 4초, 16초)
- **DLQ**: 3회 실패 시 `notification:dlq`로 이동, 수동 확인
