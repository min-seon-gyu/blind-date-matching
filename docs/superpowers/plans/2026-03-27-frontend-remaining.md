# Frontend Remaining - 주관자 + 카페주인 + 관리자 + 마켓플레이스 구현 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 주관자(데스크톱), 카페 주인(데스크톱), 관리자(데스크톱), 마켓플레이스(반응형) 웹 페이지를 모두 구현한다.

**Architecture:** 기존 단일 React 앱에 라우팅으로 추가. 데스크톱 UI는 shadcn/ui Table, Form, Dialog 기반 사이드바 레이아웃. 코랄 포인트 + 화이트/그레이 톤. 마켓플레이스는 주관자/카페주인 공용 반응형.

**Tech Stack:** React 19, TypeScript, Tailwind CSS v4, shadcn/ui, @tanstack/react-query, zustand, react-router-dom, axios

**Spec:** `docs/superpowers/specs/2026-03-23-frontend-design.md` (Section 2.4 라우팅, 2.5 디자인)

---

## Task 1: 데스크톱 레이아웃 + shadcn 추가 컴포넌트 + API 모듈

- [ ] **Step 1: shadcn 추가 컴포넌트 설치**

```bash
cd frontend
npx shadcn@latest add table badge select separator dropdown-menu sheet avatar textarea
```

- [ ] **Step 2: 데스크톱 사이드바 레이아웃**

`src/components/layout/DashboardLayout.tsx`:
- 좌측 사이드바 (로고, 네비게이션 메뉴, 로그아웃)
- 우측 메인 콘텐츠 (Outlet)
- props: `role` (organizer/cafe-owner/admin), `menuItems` 배열
- 1024px+ 최적화, 태블릿에서는 사이드바 접히는 Sheet 방식

- [ ] **Step 3: API 모듈 추가**

`src/api/organizer.ts`:
- login, getDashboard, getEvents, createEvent, updateEvent, deleteEvent, closeEvent
- getApplications, approveApplication, rejectApplication
- getEventStats, getCommissions, getPartnerships, getNotifications, markNotificationAsRead

`src/api/cafeOwner.ts`:
- login, getDashboard, getMyCafe, updateMyCafe
- getEvents, getCommissions, getPartnerships, acceptPartnership, rejectPartnership, terminatePartnership
- getNotifications, markNotificationAsRead

`src/api/marketplace.ts`:
- getPosts, createPost, getPost, updatePost, deletePost, requestPartnership, getMyPosts

`src/api/admin.ts`:
- login, getDashboard, getCafes, createCafe, getOrganizers, createOrganizer
- createCafeOwner, getCommissions, invoiceCommission, markCommissionPaid, getPartnerships

- [ ] **Step 4: 타입 추가**

`src/types/index.ts`에 추가:
- Organizer, CafeOwner, Partnership, MarketplacePost, Commission, DashboardStats 인터페이스

- [ ] **Step 5: 빌드 확인 + 커밋**

```bash
git add -A && git commit -m "feat: add dashboard layout, API modules, and extended types"
```

---

## Task 2: 주관자 페이지

- [ ] **Step 1: 주관자 로그인**

`src/pages/organizer/LoginPage.tsx`:
- 이메일/비밀번호 로그인 폼 (shadcn Input, Button)
- 깔끔한 화이트 배경 + 코랄 포인트

- [ ] **Step 2: 주관자 대시보드**

`src/pages/organizer/DashboardPage.tsx`:
- 통계 카드 (진행 중 이벤트, 총 참가자, 수수료 합계)
- 최근 이벤트 목록 (shadcn Table)

- [ ] **Step 3: 이벤트 관리**

`src/pages/organizer/EventsPage.tsx`:
- 이벤트 목록 테이블 (제목, 카페, 날짜, 상태, 정원)
- 생성 버튼 → Dialog 또는 별도 페이지

`src/pages/organizer/EventCreatePage.tsx`:
- 폼: 제휴 카페 선택(드롭다운), 제목, 날짜, 시간, 정원, 참가비, 설명, 연령 제한, 선택 수
- 제휴 카페 없으면 "먼저 마켓플레이스에서 카페와 제휴하세요" 안내

`src/pages/organizer/EventDetailPage.tsx`:
- 이벤트 정보 + 수정 폼
- 신청자 관리 탭 (승인/거절 버튼 포함 테이블)
- 이벤트 통계 탭 (매칭 현황)

- [ ] **Step 4: 제휴 관리**

`src/pages/organizer/PartnershipsPage.tsx`:
- 제휴 카페 목록 (카페명, 상태, 요청일)
- 마켓플레이스 바로가기 링크

- [ ] **Step 5: 수수료 + 알림**

`src/pages/organizer/CommissionsPage.tsx`:
- 수수료 내역 테이블 (이벤트, 금액, 상태)

`src/pages/organizer/NotificationsPage.tsx`:
- 알림 목록

- [ ] **Step 6: App.tsx에 주관자 라우트 추가**

```
/organizer/login
/organizer/dashboard
/organizer/events
/organizer/events/new
/organizer/events/:id
/organizer/partnerships
/organizer/commissions
/organizer/notifications
/organizer/marketplace
```

- [ ] **Step 7: 빌드 확인 + 커밋**

```bash
git add -A && git commit -m "feat: add organizer pages (dashboard, events, applications, partnerships)"
```

---

## Task 3: 카페 주인 페이지

- [ ] **Step 1: 카페 주인 로그인**

`src/pages/cafe-owner/LoginPage.tsx`:
- 이메일/비밀번호 로그인 폼

- [ ] **Step 2: 카페 주인 대시보드**

`src/pages/cafe-owner/DashboardPage.tsx`:
- 통계 카드 (예정 이벤트, 제휴 수, 수수료)

- [ ] **Step 3: 카페 관리**

`src/pages/cafe-owner/MyCafePage.tsx`:
- 카페 정보 수정 폼 (이름, 주소, 설명, 로고, 커버이미지)
- 카카오맵으로 위치 등록/수정 (주소 검색 → 마커 드래그 → lat/lng 저장)
- 기존 KakaoMap 컴포넌트 확장 또는 새 EditableKakaoMap 컴포넌트

- [ ] **Step 4: 제휴 관리**

`src/pages/cafe-owner/PartnershipsPage.tsx`:
- 제휴 요청 목록 (수락/거절 버튼)
- 활성 제휴 목록 (해지 버튼)

- [ ] **Step 5: 이벤트 조회 + 수수료 + 알림**

`src/pages/cafe-owner/EventsPage.tsx` — 읽기 전용 이벤트 목록
`src/pages/cafe-owner/CommissionsPage.tsx` — 수수료 내역
`src/pages/cafe-owner/NotificationsPage.tsx` — 알림

- [ ] **Step 6: App.tsx에 카페 주인 라우트 추가**

- [ ] **Step 7: 빌드 확인 + 커밋**

```bash
git add -A && git commit -m "feat: add cafe-owner pages (dashboard, cafe management, partnerships)"
```

---

## Task 4: 관리자 페이지

- [ ] **Step 1: 관리자 로그인 + 대시보드**

`src/pages/admin/LoginPage.tsx`
`src/pages/admin/DashboardPage.tsx`:
- 전체 통계 (카페 수, 주관자 수, 이벤트 수, 매칭 수, 총 수수료)

- [ ] **Step 2: 카페 + 주관자 관리**

`src/pages/admin/CafesPage.tsx`:
- 카페 목록 테이블 + 등록 Dialog (이름, 주소, slug)
- 카페 주인 계정 생성 Dialog

`src/pages/admin/OrganizersPage.tsx`:
- 주관자 목록 테이블 + 등록 Dialog (이름, 이메일, 비밀번호, 전화번호)

- [ ] **Step 3: 수수료 + 제휴 관리**

`src/pages/admin/CommissionsPage.tsx`:
- 전체 수수료 테이블 (주관자/카페 탭 분리)
- 청구/입금확인 버튼

`src/pages/admin/PartnershipsPage.tsx`:
- 전체 제휴 현황 (읽기 전용)

- [ ] **Step 4: App.tsx에 관리자 라우트 추가**

- [ ] **Step 5: 빌드 확인 + 커밋**

```bash
git add -A && git commit -m "feat: add admin pages (dashboard, cafes, organizers, commissions)"
```

---

## Task 5: 마켓플레이스 페이지

- [ ] **Step 1: 마켓플레이스 목록**

`src/pages/marketplace/MarketplacePage.tsx`:
- 탭: 전체 / 장소 제공 / 장소 구함
- 지역 필터 (드롭다운)
- 카드 그리드 (반응형: 모바일 1열, 데스크톱 2-3열)
- 글 작성 버튼

- [ ] **Step 2: 글 작성/수정**

`src/pages/marketplace/CreatePostPage.tsx`:
- 폼: 제목, 설명, 지역, 수용인원(OFFER_SPACE만), 희망날짜, 이미지
- CAFE_OWNER → OFFER_SPACE 자동 설정, ORGANIZER → SEEK_SPACE 자동 설정

`src/pages/marketplace/EditPostPage.tsx`:
- 기존 데이터 로드 + 수정

- [ ] **Step 3: 글 상세**

`src/pages/marketplace/PostDetailPage.tsx`:
- 글 정보 + 작성자 정보
- OFFER_SPACE면 카카오맵 위치 표시
- 제휴 요청 버튼 (상대 역할만)
- 본인 글이면 수정/삭제 버튼

- [ ] **Step 4: App.tsx에 마켓플레이스 라우트 추가**

```
/marketplace
/marketplace/new
/marketplace/:id
/marketplace/:id/edit
```

마켓플레이스는 ORGANIZER 또는 CAFE_OWNER 로그인 필요.

- [ ] **Step 5: 빌드 확인 + 커밋**

```bash
git add -A && git commit -m "feat: add marketplace pages (listing, create, detail, partnership request)"
```

---

## Task 6: 최종 정리

- [ ] **Step 1: 모든 빈 상태 확인**

각 테이블/목록 페이지에 EmptyState 적용 확인.

- [ ] **Step 2: 전체 빌드 확인**

```bash
cd frontend && npm run build
```

- [ ] **Step 3: 최종 커밋**

```bash
git add -A && git commit -m "chore: final cleanup and verify all pages build"
```

---

## 구현 순서 의존성

```
Task 1 (레이아웃 + API + 타입)
 ├─▶ Task 2 (주관자)
 ├─▶ Task 3 (카페 주인)
 ├─▶ Task 4 (관리자)
 └─▶ Task 5 (마켓플레이스)

Task 2~5 → Task 6 (최종 정리)
```
