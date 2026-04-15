# Blind Date Matching - B2B 소개팅 매칭 플랫폼

오프라인 소개팅 이벤트를 운영하는 카페/바에 매칭 시스템을 제공하는 B2B SaaS 플랫폼.

## 프로젝트 상태

### Phase 1: 백엔드 MVP (완료)

Bar 중심 멀티테넌트 백엔드 API 구축 완료.

- 스펙: `docs/superpowers/specs/2026-03-23-b2b-blind-date-platform-design.md`
- 구현 계획: `docs/superpowers/plans/2026-03-23-b2b-backend-implementation.md`

### Phase 2-1: 백엔드 역할 분리 (완료)

BarOwner를 CafeOwner(장소 제공) + Organizer(이벤트 운영)로 분리. Bar → Cafe 리네이밍. Commission 이원화. 4개 사용자 유형(Participant, Organizer, CafeOwner, PlatformAdmin) 기반.

- 스펙: `docs/superpowers/specs/2026-03-23-frontend-design.md` (Section 1)
- 구현 계획: `docs/superpowers/plans/2026-03-23-backend-restructure.md`

### Phase 2-2: 백엔드 마켓플레이스 (완료)

제휴 마켓플레이스 (Partnership, MarketplacePost) 추가. 이벤트 생성 시 ACTIVE Partnership 검증.

- 구현 계획: `docs/superpowers/plans/2026-03-27-backend-marketplace.md`

### Phase 2-3a: 프론트엔드 공통 + 참가자 웹 (완료)

Tailwind CSS v4 + shadcn/ui 설정. 참가자 전체 10개 페이지 (로그인, 프로필, 카페, 이벤트, 선택, 매칭, 마이페이지, 알림). 카카오맵 연동.

- 구현 계획: `docs/superpowers/plans/2026-03-27-frontend-participant.md`

### Phase 2-3b: 프론트엔드 주관자 + 카페 주인 + 관리자 + 마켓플레이스 (완료)

주관자 8페이지, 카페 주인 7페이지, 관리자 6페이지, 마켓플레이스 4페이지. 데스크톱 사이드바 레이아웃.

- 구현 계획: `docs/superpowers/plans/2026-03-27-frontend-remaining.md`

React 19 + shadcn/ui + Tailwind CSS. 참가자(모바일) + 주관자/카페주인/관리자(데스크톱) 전체 구현.

- 스펙: `docs/superpowers/specs/2026-03-23-frontend-design.md` (Section 2)

## 기술 스택

| 구분 | 기술 |
|------|------|
| Backend | Kotlin 1.9, Spring Boot 3.2, Spring Data JPA, Spring Security |
| Frontend | React 19, TypeScript 5.9, Vite 8, shadcn/ui, Tailwind CSS, TanStack Query 5, Zustand 5 |
| Database | MySQL 8.0, Redis 7 |
| 인증 | JWT (jjwt 0.12) + 카카오 OAuth 2.0 |
| 알림 | 카카오 채널 메시지 API |
| 파일 저장 | AWS S3 (Presigned URL) |
| 지도 | 카카오맵 JavaScript SDK |
| 인프라 | Docker Compose, Nginx |

## 아키텍처

```
[참가자 웹 (모바일)]  ──nginx──▶ [Spring Boot API] ──▶ [MySQL]
[주관자 웹 (데스크톱)]                  │
[카페 주인 웹 (데스크톱)]               ├──▶ [Redis]
[관리자 웹 (데스크톱)]                  ├──▶ [카카오 OAuth]
                                       ├──▶ [카카오 채널 메시지 API]
                                       ├──▶ [카카오맵 API]
                                       └──▶ [AWS S3]
```

### 사용자 유형

| 역할 | 설명 | 인증 방식 |
|------|------|----------|
| Participant (참가자) | 이벤트 신청, 선택, 매칭 결과 확인 | 카카오 OAuth |
| Organizer (주관자) | 이벤트 기획/운영, 신청자 관리 | 이메일/비밀번호 |
| CafeOwner (카페 주인) | 장소 제공, 마켓플레이스 글 등록 | 이메일/비밀번호 |
| PlatformAdmin (관리자) | 전체 관리, 수수료 관리 | 이메일/비밀번호 |

## 프로젝트 구조

```
blind-date-matching/
├── backend/
│   └── src/main/kotlin/com/blinddate/
│       ├── auth/              # JWT 인증 (4개 사용자 유형)
│       ├── participant/       # 참가자 프로필
│       ├── cafe/              # 카페 엔티티 + 공개 API
│       ├── cafeowner/         # 카페 주인 관리 (장소 제공)
│       ├── organizer/         # 주관자 관리 (이벤트 운영)
│       ├── event/             # 이벤트 엔티티 + 공개 API
│       ├── application/       # 참가 신청/승인/거절
│       ├── matching/          # 참가번호, 선택, 매칭 알고리즘
│       ├── notification/      # 알림 (DB + 카카오 채널 메시지)
│       ├── action/            # ActionToken (카톡 버튼 액션)
│       ├── partnership/        # 제휴 관리 (카페-주관자 연결)
│       ├── marketplace/       # 마켓플레이스 (장소 제공/구함 글)
│       ├── commission/        # 수수료 추적
│       ├── admin/             # 플랫폼 관리자 API
│       ├── scheduler/         # 이벤트 상태 전환, 매칭, 리마인더
│       └── common/            # 설정, 공통 엔티티, 예외 처리
├── frontend/                  # React SPA (Phase 2-3에서 전면 구현)
├── docs/
│   ├── superpowers/specs/     # 설계 문서
│   └── superpowers/plans/     # 구현 계획
└── docker-compose.yml
```

## 환경 설정

### 사전 요구사항

- Docker & Docker Compose
- (로컬 개발 시) JDK 17, Node.js 20+

### Docker Compose로 실행

```bash
docker compose up -d
```

| 서비스 | 포트 |
|--------|------|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| MySQL | localhost:3306 |
| Redis | localhost:6379 |

### 로컬 개발 환경

**백엔드:**
```bash
cd backend
./gradlew bootRun
```

**프론트엔드:**
```bash
cd frontend
npm install
npm run dev
```

### 환경 변수

| 변수 | 설명 | 기본값 |
|------|------|--------|
| `KAKAO_CLIENT_ID` | 카카오 앱 REST API 키 | `test` |
| `KAKAO_CHANNEL_ADMIN_KEY` | 카카오 채널 관리자 키 | - |
| `AWS_S3_BUCKET` | S3 버킷 이름 | `blinddate-local` |
| `JWT_SECRET` | JWT 서명 키 | 로컬 개발용 기본값 |

## 테스트

```bash
cd backend
./gradlew test
```

MockK 기반 단위 테스트. H2 인메모리 DB (테스트 환경).

## 라이선스

Private
