# Frontend Common + Participant Web - 구현 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 프론트엔드 인프라(Tailwind + shadcn/ui)를 설정하고, 참가자 웹(모바일 최적화)의 전체 페이지를 구현한다.

**Architecture:** 기존 프론트엔드 소스를 전면 교체한다. 단일 React 앱에서 라우팅으로 역할을 분리하며, 이 계획에서는 참가자 웹과 공통 인프라만 구현한다. 디자인은 따뜻한 & 캐주얼 (코랄/옐로우 그라데이션, 라운드 UI).

**Tech Stack:** React 19, TypeScript 5.9, Vite 8, Tailwind CSS, shadcn/ui, @tanstack/react-query 5, zustand 5, react-router-dom 7, axios, 카카오맵 JS SDK

**Spec:** `docs/superpowers/specs/2026-03-23-frontend-design.md` (Section 2: 프론트엔드 설계)

---

## Task 1: 프론트엔드 초기 설정 (Tailwind + shadcn/ui + 프로젝트 정리)

기존 소스를 정리하고 Tailwind CSS + shadcn/ui를 설정한다.

- [ ] **Step 1: 기존 소스 정리**

기존 `src/` 하위의 모든 파일을 삭제하고 새로 시작:
```bash
cd frontend
rm -rf src/pages/ src/components/ src/api/ src/hooks/ src/stores/ src/types/ src/styles/
rm -f src/App.tsx
```

`src/main.tsx`는 유지 (나중에 수정).

- [ ] **Step 2: Tailwind CSS 설치 + 설정**

```bash
cd frontend
npm install -D tailwindcss @tailwindcss/vite
```

`src/styles/global.css` 재생성:
```css
@import "tailwindcss";
```

`vite.config.ts` 업데이트:
```typescript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import path from 'path'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
})
```

`tsconfig.app.json`에 paths 추가:
```json
"paths": {
  "@/*": ["./src/*"]
}
```

- [ ] **Step 3: shadcn/ui 초기화**

```bash
cd frontend
npx shadcn@latest init
```

설정:
- Style: New York
- Base color: Neutral
- CSS variables: Yes

설치 후 필요한 컴포넌트 추가:
```bash
npx shadcn@latest add button card input label dialog tabs toast
```

- [ ] **Step 4: 컬러 팔레트 커스텀 (코랄/옐로우 테마)**

shadcn의 CSS 변수를 스펙의 컬러 팔레트로 오버라이드. `src/styles/global.css`에 추가:
```css
@layer base {
  :root {
    --primary: 0 84.2% 60.2%;       /* #FF6B6B 코랄 */
    --primary-foreground: 0 0% 100%;
    --secondary: 45 100% 71%;        /* #FFE66D 옐로우 */
    --background: 0 100% 98%;        /* #FFF5F5 */
    /* ... shadcn 기본 변수 오버라이드 */
  }
}
```

- [ ] **Step 5: index.html 업데이트**

title을 "소개팅 매칭"으로 변경. 카카오맵 SDK 스크립트 추가:
```html
<script type="text/javascript" src="//dapi.kakao.com/v2/maps/sdk.js?appkey=YOUR_APP_KEY&libraries=services"></script>
```

- [ ] **Step 6: 빌드 확인**

```bash
cd frontend && npm run build
```

- [ ] **Step 7: 커밋**

```bash
git add -A && git commit -m "feat: setup Tailwind CSS + shadcn/ui + project cleanup"
```

---

## Task 2: 공통 인프라 (API 클라이언트, 타입, 스토어, 라우팅)

- [ ] **Step 1: TypeScript 타입 정의**

`src/types/index.ts` — 스펙의 2.7절 전체 타입 정의 (UserType, Gender, EventStatus, Cafe, Event, Application, ParticipantProfile, Notification, MatchResultResponse, ParticipantInfo 등)

- [ ] **Step 2: API 클라이언트**

`src/api/client.ts` — axios 인스턴스 + JWT 인터셉터 + 401 자동 refresh
`src/api/auth.ts` — kakaoLogin, refresh
`src/api/participant.ts` — getMe, getProfile, createProfile, updateProfile, getMyApplications, getNotifications, markNotificationAsRead
`src/api/event.ts` — getCafe, getCafeEvents, getEvent, applyEvent, cancelApplication, getParticipants, submitChoices, updateChoices, getMatchResult

- [ ] **Step 3: Auth 스토어**

`src/stores/authStore.ts`:
```typescript
type UserType = 'PARTICIPANT' | 'ORGANIZER' | 'CAFE_OWNER' | 'PLATFORM_ADMIN'

interface AuthUser {
  id: number
  userType: UserType
  hasProfile?: boolean
  cafeId?: number
}

interface AuthState {
  accessToken: string | null
  user: AuthUser | null
  setAuth: (token: string, user: AuthUser) => void
  logout: () => void
}
```

- [ ] **Step 4: App.tsx + 라우팅 구조**

`src/App.tsx`:
- QueryClientProvider + BrowserRouter
- 참가자 라우트: `/login`, `/auth/kakao/callback`, `/profile/setup`, `/cafes/{slug}`, `/cafes/{slug}/events/{id}`, `/me`, `/me/profile`, `/me/applications`, `/me/notifications`
- 나중에 추가될 라우트를 위한 주석 자리 (organizer, cafe-owner, admin, marketplace)

- [ ] **Step 5: ProtectedRoute 컴포넌트**

`src/components/common/ProtectedRoute.tsx` — 로그인 체크 + userType 체크 + 프로필 미작성 시 리다이렉트

- [ ] **Step 6: main.tsx 업데이트**

global.css import 확인.

- [ ] **Step 7: 빌드 확인 + 커밋**

```bash
git add -A && git commit -m "feat: add common infrastructure (types, API client, auth store, routing)"
```

---

## Task 3: 참가자 레이아웃 + 로그인

- [ ] **Step 1: 참가자 레이아웃**

`src/components/layout/ParticipantLayout.tsx`:
- 상단 헤더 (로고, 알림 아이콘)
- 하단 네비게이션 바 (홈, 내 신청, 마이페이지)
- Outlet으로 페이지 렌더

모바일 최적화: max-w-md mx-auto, min-h-screen

- [ ] **Step 2: 로그인 페이지**

`src/pages/participant/LoginPage.tsx`:
- 따뜻한 그라데이션 배경
- 앱 로고/타이틀
- 카카오 로그인 버튼 (카카오 OAuth URL로 리다이렉트)

- [ ] **Step 3: 카카오 콜백 페이지**

`src/pages/participant/KakaoCallbackPage.tsx`:
- URL에서 code 파라미터 추출
- `POST /api/auth/kakao` 호출
- 성공 시 authStore에 토큰 저장 + 프로필 여부에 따라 리다이렉트

- [ ] **Step 4: 빌드 확인 + 커밋**

```bash
git add -A && git commit -m "feat: add participant layout and login flow"
```

---

## Task 4: 프로필 설정/수정

- [ ] **Step 1: 프로필 설정 페이지**

`src/pages/participant/ProfileSetupPage.tsx`:
- 필수: 이름, 나이, 성별, 직업
- 선택: 키, MBTI, 취미, 음주, 흡연, 종교, 이상형, 자기소개, 프로필 사진
- 프로필 사진 업로드: `/api/upload/presigned-url` → S3 업로드
- 제출 시 `POST /api/me/profile`

- [ ] **Step 2: 프로필 수정 페이지**

`src/pages/participant/ProfileEditPage.tsx`:
- 기존 프로필 데이터 로드 (`GET /api/me/profile`)
- 동일한 폼, `PUT /api/me/profile`

- [ ] **Step 3: 커밋**

```bash
git add -A && git commit -m "feat: add profile setup and edit pages"
```

---

## Task 5: 카페 메인 + 이벤트 상세

- [ ] **Step 1: 카페 메인 페이지**

`src/pages/participant/CafeMainPage.tsx` (`/cafes/{slug}`):
- 그라데이션 헤더 (카페 이름, 설명)
- 이벤트 카드 리스트 (라운드 카드, 정원 현황 프로그레스 바)
- OPEN 이벤트만 신청 가능, CLOSED/COMPLETED는 흐리게 표시

`src/components/participant/EventCard.tsx`:
- 이벤트 제목, 날짜, 시간, 참가비
- 남녀 정원 현황 (프로그레스 바)
- 상태 뱃지 (모집중/마감/완료)

- [ ] **Step 2: 이벤트 상세 페이지**

`src/pages/participant/EventDetailPage.tsx` (`/cafes/{slug}/events/{id}`):
- 이벤트 정보 (제목, 날짜, 시간, 참가비, 설명, 정원)
- 카카오맵 (카페 위치 표시)
- 출발지 설정 (기본: 현재 GPS, 수정 가능)
- 경로 안내 (카카오맵 길찾기 URL 링크)
- 신청하기 버튼 (OPEN 상태일 때만)

`src/components/common/KakaoMap.tsx`:
- 카카오맵 공통 컴포넌트 (위치 표시, 마커)

- [ ] **Step 3: 이벤트 신청 처리**

신청하기 버튼 클릭 → `POST /api/events/{id}/apply` → 성공 시 "신청 완료" 토스트 + 내 신청으로 이동

- [ ] **Step 4: 커밋**

```bash
git add -A && git commit -m "feat: add cafe main page and event detail with Kakao Map"
```

---

## Task 6: 선택 + 매칭 결과

- [ ] **Step 1: 선택 페이지**

`src/pages/participant/ChoicePage.tsx` (`/cafes/{slug}/events/{id}/choose`):
- 이성 참가자 번호 목록 (`GET /api/events/{id}/participants`)
- 각 참가자: 번호, 나이, 직업, 한줄 소개
- 선택/해제 토글 (maxChoices 이내)
- 선택 마감 시간 카운트다운
- 제출 버튼 → `POST /api/events/{id}/choices`

- [ ] **Step 2: 매칭 결과 페이지**

`src/pages/participant/MatchResultPage.tsx` (`/cafes/{slug}/events/{id}/result`):
- 매칭 성공: 축하 애니메이션 + 매칭된 상대 닉네임
- 매칭 실패: 위로 메시지
- `GET /api/events/{id}/result`

- [ ] **Step 3: 커밋**

```bash
git add -A && git commit -m "feat: add choice page and match result page"
```

---

## Task 7: 마이페이지 + 신청 내역 + 알림

- [ ] **Step 1: 마이페이지**

`src/pages/participant/MyPage.tsx` (`/me`):
- 프로필 요약 (이름, 사진)
- 메뉴: 프로필 수정, 신청 내역, 알림, 로그아웃

- [ ] **Step 2: 신청 내역 페이지**

`src/pages/participant/MyApplicationsPage.tsx` (`/me/applications`):
- 신청 목록 (이벤트명, 카페명, 날짜, 상태 뱃지)
- 상태별 색상: PENDING(회색), APPROVED(초록), REJECTED(빨강), CANCELLED(회색)
- PENDING 상태에서 취소 가능

- [ ] **Step 3: 알림 페이지**

`src/pages/participant/NotificationsPage.tsx` (`/me/notifications`):
- 알림 목록 (타입 아이콘, 제목, 메시지, 시간)
- 읽음/안읽음 구분
- 클릭 시 읽음 처리 (`PUT /api/notifications/{id}/read`)

- [ ] **Step 4: 커밋**

```bash
git add -A && git commit -m "feat: add my page, applications, and notifications"
```

---

## Task 8: 빈 상태 + 에러 처리 + 최종 정리

- [ ] **Step 1: EmptyState 공통 컴포넌트**

`src/components/common/EmptyState.tsx`:
- 아이콘 + 메시지 + 선택적 액션 버튼
- 각 페이지에 적용

- [ ] **Step 2: 에러 바운더리 + 토스트**

`src/components/common/ErrorBoundary.tsx`
토스트: shadcn/ui toast 활용

- [ ] **Step 3: 로딩 상태**

각 데이터 페칭 페이지에 로딩 스피너 추가.

- [ ] **Step 4: 전체 빌드 확인**

```bash
cd frontend && npm run build
```

- [ ] **Step 5: 커밋**

```bash
git add -A && git commit -m "feat: add empty states, error handling, and loading states"
```

---

## 구현 순서 의존성

```
Task 1 (Tailwind + shadcn 설정)
 └─▶ Task 2 (공통 인프라)
      └─▶ Task 3 (레이아웃 + 로그인)
           ├─▶ Task 4 (프로필)
           ├─▶ Task 5 (카페 + 이벤트)
           │    └─▶ Task 6 (선택 + 매칭)
           └─▶ Task 7 (마이페이지)

Task 3~7 → Task 8 (빈 상태/에러 처리)
```
