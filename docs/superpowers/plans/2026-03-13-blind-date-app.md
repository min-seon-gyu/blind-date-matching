# 블라인드 소개팅 + 혼술바 웹앱 구현 계획

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 그룹 소개팅 매칭 + 혼술바 실시간 현황을 제공하는 모바일 최적화 웹앱 구축

**Architecture:** Spring Boot + Kotlin 모듈형 모놀리스 백엔드와 React 프론트엔드. 카카오 OAuth2 로그인, 토스페이먼츠 결제, 카카오 알림톡 연동. Redis로 세션/캐싱/알림큐/혼술바 실시간 현황 관리.

**Tech Stack:** Kotlin, Spring Boot 3, Spring Data JPA, Spring Security, MySQL, Redis, React 18, TypeScript, Vite, React Router, Axios, TanStack Query

---

## File Structure

### Backend (`backend/`)

```
backend/
├── build.gradle.kts
├── settings.gradle.kts
├── src/main/kotlin/com/blinddate/
│   ├── BlindDateApplication.kt
│   ├── common/
│   │   ├── entity/BaseEntity.kt              # id, createdAt, updatedAt
│   │   ├── exception/GlobalExceptionHandler.kt
│   │   ├── exception/BusinessException.kt
│   │   ├── config/JpaConfig.kt
│   │   ├── config/RedisConfig.kt
│   │   ├── config/WebSecurityConfig.kt
│   │   └── config/S3Config.kt
│   ├── auth/
│   │   ├── controller/AuthController.kt
│   │   ├── service/AuthService.kt
│   │   ├── service/KakaoOAuthService.kt
│   │   ├── jwt/JwtTokenProvider.kt
│   │   ├── jwt/JwtAuthenticationFilter.kt
│   │   ├── dto/AuthDtos.kt                   # LoginRequest, TokenResponse, KakaoUserInfo
│   │   └── repository/RefreshTokenRepository.kt  # Redis
│   ├── member/
│   │   ├── controller/MemberController.kt
│   │   ├── service/MemberService.kt
│   │   ├── service/ProfileImageService.kt
│   │   ├── entity/Member.kt
│   │   ├── entity/MemberProfile.kt
│   │   ├── entity/Role.kt                    # enum
│   │   ├── entity/Gender.kt                  # enum
│   │   ├── entity/DrinkingType.kt            # enum
│   │   ├── entity/SmokingType.kt             # enum
│   │   ├── repository/MemberRepository.kt
│   │   ├── repository/MemberProfileRepository.kt
│   │   └── dto/MemberDtos.kt
│   ├── event/
│   │   ├── controller/EventController.kt
│   │   ├── controller/AdminEventController.kt
│   │   ├── service/EventService.kt
│   │   ├── entity/BlindDateEvent.kt
│   │   ├── entity/EventStatus.kt             # enum
│   │   ├── repository/BlindDateEventRepository.kt
│   │   └── dto/EventDtos.kt
│   ├── application/
│   │   ├── controller/ApplicationController.kt
│   │   ├── controller/AdminApplicationController.kt
│   │   ├── service/ApplicationService.kt
│   │   ├── entity/Application.kt
│   │   ├── entity/ApplicationStatus.kt       # enum
│   │   ├── repository/ApplicationRepository.kt
│   │   └── dto/ApplicationDtos.kt
│   ├── payment/
│   │   ├── controller/PaymentController.kt
│   │   ├── service/PaymentService.kt
│   │   ├── service/TossPaymentsClient.kt
│   │   ├── entity/Payment.kt
│   │   ├── entity/PaymentStatus.kt           # enum
│   │   ├── repository/PaymentRepository.kt
│   │   └── dto/PaymentDtos.kt
│   ├── matching/
│   │   ├── controller/MatchingController.kt
│   │   ├── service/MatchingService.kt
│   │   ├── service/ParticipantNumberService.kt
│   │   ├── entity/ParticipantNumber.kt
│   │   ├── entity/Choice.kt
│   │   ├── entity/MatchResult.kt
│   │   ├── repository/ParticipantNumberRepository.kt
│   │   ├── repository/ChoiceRepository.kt
│   │   ├── repository/MatchResultRepository.kt
│   │   └── dto/MatchingDtos.kt
│   ├── notification/
│   │   ├── controller/NotificationController.kt
│   │   ├── service/NotificationService.kt
│   │   ├── service/KakaoAlimtalkService.kt
│   │   ├── service/WebhookService.kt
│   │   ├── service/NotificationQueueConsumer.kt
│   │   ├── entity/Notification.kt
│   │   ├── entity/NotificationType.kt        # enum
│   │   ├── repository/NotificationRepository.kt
│   │   └── dto/NotificationDtos.kt
│   ├── bar/
│   │   ├── controller/BarController.kt
│   │   ├── controller/AdminBarController.kt
│   │   ├── service/BarService.kt
│   │   ├── service/BarStatusRedisService.kt
│   │   ├── entity/Bar.kt
│   │   ├── entity/BarReservation.kt
│   │   ├── entity/BarReservationStatus.kt    # enum
│   │   ├── entity/BarVisitLog.kt
│   │   ├── repository/BarRepository.kt
│   │   ├── repository/BarReservationRepository.kt
│   │   ├── repository/BarVisitLogRepository.kt
│   │   └── dto/BarDtos.kt
│   └── scheduler/
│       ├── MatchingScheduler.kt
│       ├── EventStatusScheduler.kt
│       └── ReminderScheduler.kt
├── src/main/resources/
│   ├── application.yml
│   ├── application-local.yml
│   └── application-test.yml
└── src/test/kotlin/com/blinddate/
    ├── auth/service/AuthServiceTest.kt
    ├── member/service/MemberServiceTest.kt
    ├── event/service/EventServiceTest.kt
    ├── application/service/ApplicationServiceTest.kt
    ├── payment/service/PaymentServiceTest.kt
    ├── matching/service/MatchingServiceTest.kt
    ├── notification/service/NotificationServiceTest.kt
    ├── bar/service/BarServiceTest.kt
    └── scheduler/MatchingSchedulerTest.kt
```

### Frontend (`frontend/`)

```
frontend/
├── package.json
├── vite.config.ts
├── tsconfig.json
├── index.html
├── public/
├── src/
│   ├── main.tsx
│   ├── App.tsx
│   ├── api/
│   │   ├── client.ts                  # Axios 인스턴스, 인터셉터
│   │   ├── auth.ts
│   │   ├── member.ts
│   │   ├── event.ts
│   │   ├── application.ts
│   │   ├── payment.ts
│   │   ├── matching.ts
│   │   ├── notification.ts
│   │   └── bar.ts
│   ├── hooks/
│   │   ├── useAuth.ts
│   │   ├── useEvents.ts
│   │   ├── useApplications.ts
│   │   ├── useNotifications.ts
│   │   ├── useBar.ts
│   │   └── useMatching.ts
│   ├── stores/
│   │   └── authStore.ts               # Zustand
│   ├── pages/
│   │   ├── LoginPage.tsx
│   │   ├── ProfileSetupPage.tsx
│   │   ├── CalendarPage.tsx
│   │   ├── EventDetailPage.tsx
│   │   ├── ChoicePage.tsx
│   │   ├── MatchResultPage.tsx
│   │   ├── MyPage.tsx
│   │   ├── MyApplicationsPage.tsx
│   │   ├── ProfileEditPage.tsx
│   │   ├── BarStatusPage.tsx
│   │   ├── BarReservePage.tsx
│   │   ├── NotificationsPage.tsx
│   │   ├── admin/
│   │   │   ├── AdminEventsPage.tsx
│   │   │   ├── AdminApplicationsPage.tsx
│   │   │   ├── AdminMembersPage.tsx
│   │   │   └── AdminBarPage.tsx
│   │   └── KakaoCallbackPage.tsx
│   ├── components/
│   │   ├── layout/
│   │   │   ├── AppLayout.tsx
│   │   │   ├── BottomNav.tsx
│   │   │   └── Header.tsx
│   │   ├── calendar/
│   │   │   └── Calendar.tsx
│   │   ├── event/
│   │   │   ├── EventCard.tsx
│   │   │   └── CapacityBar.tsx
│   │   ├── bar/
│   │   │   └── BarStatusCard.tsx
│   │   ├── common/
│   │   │   ├── Button.tsx
│   │   │   ├── Input.tsx
│   │   │   ├── Modal.tsx
│   │   │   └── Badge.tsx
│   │   └── auth/
│   │       └── ProtectedRoute.tsx
│   ├── styles/
│   │   └── global.css
│   └── types/
│       └── index.ts                    # 모든 TypeScript 타입 정의
```

---

## Chunk 1: 프로젝트 초기 설정 + Common 모듈

### Task 1: Backend 프로젝트 생성

**Files:**
- Create: `backend/build.gradle.kts`
- Create: `backend/settings.gradle.kts`
- Create: `backend/src/main/kotlin/com/blinddate/BlindDateApplication.kt`
- Create: `backend/src/main/resources/application.yml`
- Create: `backend/src/main/resources/application-local.yml`
- Create: `backend/src/main/resources/application-test.yml`

- [ ] **Step 1: build.gradle.kts 작성**

```kotlin
// backend/build.gradle.kts
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
    kotlin("plugin.jpa") version "1.9.22"
    kotlin("kapt") version "1.9.22"
}

group = "com.blinddate"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")

    // Database
    runtimeOnly("com.mysql:mysql-connector-j")

    // AWS S3
    implementation("software.amazon.awssdk:s3:2.24.0")

    // WebClient (for Kakao/Toss API)
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("io.mockk:mockk:1.13.9")
    testRuntimeOnly("com.h2database:h2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}
```

- [ ] **Step 2: settings.gradle.kts 작성**

```kotlin
// backend/settings.gradle.kts
rootProject.name = "blind-date-matching"
```

- [ ] **Step 3: Application 메인 클래스 작성**

```kotlin
// backend/src/main/kotlin/com/blinddate/BlindDateApplication.kt
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

- [ ] **Step 4: application.yml 설정 파일 작성**

```yaml
# backend/src/main/resources/application.yml
spring:
  profiles:
    active: local
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true
    open-in-view: false
  jackson:
    property-naming-strategy: SNAKE_CASE

server:
  port: 8080
```

```yaml
# backend/src/main/resources/application-local.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/blinddate?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  data:
    redis:
      host: localhost
      port: 6379

jwt:
  secret: local-dev-secret-key-must-be-at-least-256-bits-long-for-hs256
  access-token-expiry: 1800000    # 30min
  refresh-token-expiry: 1209600000 # 14days

kakao:
  client-id: ${KAKAO_CLIENT_ID:test}
  redirect-uri: http://localhost:5173/auth/kakao/callback

toss:
  secret-key: ${TOSS_SECRET_KEY:test}
  base-url: https://api.tosspayments.com

aws:
  s3:
    bucket: ${AWS_S3_BUCKET:blinddate-local}
    region: ap-northeast-2
```

```yaml
# backend/src/main/resources/application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    database-platform: org.hibernate.dialect.H2Dialect
  data:
    redis:
      host: localhost
      port: 6379

jwt:
  secret: test-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm
  access-token-expiry: 1800000
  refresh-token-expiry: 1209600000

kakao:
  client-id: test-client-id
  redirect-uri: http://localhost:5173/auth/kakao/callback

toss:
  secret-key: test-secret-key
  base-url: https://api.tosspayments.com
```

- [ ] **Step 5: Commit**

```bash
git add backend/
git commit -m "feat: initialize Spring Boot + Kotlin backend project"
```

### Task 2: Common 모듈 (BaseEntity, 예외처리, Config)

**Files:**
- Create: `backend/src/main/kotlin/com/blinddate/common/entity/BaseEntity.kt`
- Create: `backend/src/main/kotlin/com/blinddate/common/exception/BusinessException.kt`
- Create: `backend/src/main/kotlin/com/blinddate/common/exception/GlobalExceptionHandler.kt`
- Create: `backend/src/main/kotlin/com/blinddate/common/config/JpaConfig.kt`
- Create: `backend/src/main/kotlin/com/blinddate/common/config/RedisConfig.kt`

- [ ] **Step 1: BaseEntity 작성**

```kotlin
// backend/src/main/kotlin/com/blinddate/common/entity/BaseEntity.kt
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
    @Column(updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @LastModifiedDate
    var updatedAt: LocalDateTime = LocalDateTime.now()
}
```

- [ ] **Step 2: 예외 클래스 작성**

```kotlin
// backend/src/main/kotlin/com/blinddate/common/exception/BusinessException.kt
package com.blinddate.common.exception

import org.springframework.http.HttpStatus

open class BusinessException(
    val status: HttpStatus,
    override val message: String
) : RuntimeException(message)

class NotFoundException(message: String) : BusinessException(HttpStatus.NOT_FOUND, message)
class BadRequestException(message: String) : BusinessException(HttpStatus.BAD_REQUEST, message)
class ForbiddenException(message: String) : BusinessException(HttpStatus.FORBIDDEN, message)
class UnauthorizedException(message: String) : BusinessException(HttpStatus.UNAUTHORIZED, message)
class ConflictException(message: String) : BusinessException(HttpStatus.CONFLICT, message)
```

```kotlin
// backend/src/main/kotlin/com/blinddate/common/exception/GlobalExceptionHandler.kt
package com.blinddate.common.exception

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

data class ErrorResponse(val message: String)

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(e.status).body(ErrorResponse(e.message))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val message = e.bindingResult.fieldErrors.joinToString(", ") {
            "${it.field}: ${it.defaultMessage}"
        }
        return ResponseEntity.badRequest().body(ErrorResponse(message))
    }
}
```

- [ ] **Step 3: Config 클래스 작성**

```kotlin
// backend/src/main/kotlin/com/blinddate/common/config/JpaConfig.kt
package com.blinddate.common.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@Configuration
@EnableJpaAuditing
class JpaConfig
```

```kotlin
// backend/src/main/kotlin/com/blinddate/common/config/RedisConfig.kt
package com.blinddate.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {

    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, String> {
        return RedisTemplate<String, String>().apply {
            setConnectionFactory(connectionFactory)
            keySerializer = StringRedisSerializer()
            valueSerializer = StringRedisSerializer()
            hashKeySerializer = StringRedisSerializer()
            hashValueSerializer = StringRedisSerializer()
        }
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/kotlin/com/blinddate/common/
git commit -m "feat: add common module - BaseEntity, exceptions, configs"
```

### Task 3: Frontend 프로젝트 생성

**Files:**
- Create: `frontend/package.json`
- Create: `frontend/vite.config.ts`
- Create: `frontend/tsconfig.json`
- Create: `frontend/index.html`
- Create: `frontend/src/main.tsx`
- Create: `frontend/src/App.tsx`
- Create: `frontend/src/styles/global.css`
- Create: `frontend/src/types/index.ts`

- [ ] **Step 1: Vite + React + TypeScript 프로젝트 생성**

```bash
cd frontend
npm create vite@latest . -- --template react-ts
```

- [ ] **Step 2: 의존성 설치**

```bash
npm install axios react-router-dom zustand @tanstack/react-query
npm install -D @types/react-router-dom
```

- [ ] **Step 3: global.css 작성**

```css
/* frontend/src/styles/global.css */
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;600;700&display=swap');

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Noto Sans KR', sans-serif;
  background: #fafafa;
  color: #333;
  max-width: 430px;
  margin: 0 auto;
  min-height: 100vh;
}

:root {
  --primary: #ff6b8a;
  --primary-dark: #ff4d73;
  --secondary: #ff8e53;
  --gradient: linear-gradient(135deg, #ff6b8a 0%, #ff8e53 100%);
  --admin-gradient: linear-gradient(135deg, #6366f1, #8b5cf6);
  --bg: #fafafa;
  --card-bg: #ffffff;
  --text: #333333;
  --text-light: #999999;
  --border: #f0f0f0;
  --radius: 14px;
  --shadow: 0 2px 12px rgba(0,0,0,0.06);
}
```

- [ ] **Step 4: TypeScript 타입 정의**

```typescript
// frontend/src/types/index.ts
export type Role = 'USER' | 'ADMIN'
export type Gender = 'MALE' | 'FEMALE'
export type DrinkingType = 'NONE' | 'SOMETIMES' | 'OFTEN'
export type SmokingType = 'NONE' | 'SOMETIMES' | 'OFTEN'
export type EventStatus = 'OPEN' | 'CLOSED' | 'COMPLETED'
export type ApplicationStatus = 'PAYMENT_WAITING' | 'PAID' | 'APPROVED' | 'REJECTED' | 'CANCELLED' | 'COMPLETED'
export type PaymentStatus = 'PENDING' | 'PAID' | 'REFUNDED' | 'FAILED'
export type BarReservationStatus = 'CONFIRMED' | 'CANCELLED' | 'VISITED' | 'NO_SHOW'
export type NotificationType = 'NEW_APPLICATION' | 'APPROVED' | 'REJECTED' | 'MATCH_RESULT' | 'EVENT_REMINDER'

export interface Member {
  id: number
  kakaoId: string
  email: string
  nickname: string
  phoneNumber: string
  role: Role
}

export interface MemberProfile {
  id: number
  memberId: number
  name: string
  age: number
  gender: Gender
  job: string
  height: number
  mbti: string
  hobby: string
  drinking: DrinkingType
  smoking: SmokingType
  religion: string
  idealType: string
  introduction: string
  photoUrl: string
}

export interface BlindDateEvent {
  id: number
  title: string
  date: string
  time: string
  maleCapacity: number
  femaleCapacity: number
  currentMaleCount: number
  currentFemaleCount: number
  price: number
  status: EventStatus
  description: string
  choiceDeadline: string
  minAge: number | null
  maxAge: number | null
}

export interface Application {
  id: number
  memberId: number
  eventId: number
  status: ApplicationStatus
  appliedAt: string
  reviewedAt: string | null
  rejectReason: string | null
}

export interface Payment {
  id: number
  applicationId: number
  amount: number
  paymentKey: string
  orderId: string
  status: PaymentStatus
  paidAt: string | null
}

export interface ParticipantInfo {
  number: number
  gender: Gender
}

export interface MatchResultResponse {
  matched: boolean
  partnerNumber: number | null
  notifiedAt: string | null
}

export interface Notification {
  id: number
  type: NotificationType
  title: string
  message: string
  isRead: boolean
  createdAt: string
}

export interface BarStatus {
  id: number
  name: string
  address: string
  totalSeats: number
  currentMaleCount: number
  currentFemaleCount: number
  remainingSeats: number
  isOpen: boolean
  openTime: string
  closeTime: string
}

export interface BarReservation {
  id: number
  barId: number
  date: string
  time: string
  status: BarReservationStatus
  createdAt: string
}
```

- [ ] **Step 5: App.tsx 라우팅 기본 구조**

```tsx
// frontend/src/App.tsx
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'

const queryClient = new QueryClient()

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<div>Login</div>} />
          <Route path="/" element={<div>Calendar</div>} />
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  )
}

export default App
```

- [ ] **Step 6: main.tsx 수정**

```tsx
// frontend/src/main.tsx
import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App'
import './styles/global.css'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
)
```

- [ ] **Step 7: Commit**

```bash
git add frontend/
git commit -m "feat: initialize React + TypeScript frontend project"
```

---

## Chunk 2: Auth 모듈 (카카오 로그인 + JWT)

### Task 4: Member Entity + Repository

**Files:**
- Create: `backend/src/main/kotlin/com/blinddate/member/entity/Role.kt`
- Create: `backend/src/main/kotlin/com/blinddate/member/entity/Gender.kt`
- Create: `backend/src/main/kotlin/com/blinddate/member/entity/DrinkingType.kt`
- Create: `backend/src/main/kotlin/com/blinddate/member/entity/SmokingType.kt`
- Create: `backend/src/main/kotlin/com/blinddate/member/entity/Member.kt`
- Create: `backend/src/main/kotlin/com/blinddate/member/entity/MemberProfile.kt`
- Create: `backend/src/main/kotlin/com/blinddate/member/repository/MemberRepository.kt`
- Create: `backend/src/main/kotlin/com/blinddate/member/repository/MemberProfileRepository.kt`

- [ ] **Step 1: Enum 클래스 작성**

```kotlin
// Role.kt
package com.blinddate.member.entity
enum class Role { USER, ADMIN }

// Gender.kt
package com.blinddate.member.entity
enum class Gender { MALE, FEMALE }

// DrinkingType.kt
package com.blinddate.member.entity
enum class DrinkingType { NONE, SOMETIMES, OFTEN }

// SmokingType.kt
package com.blinddate.member.entity
enum class SmokingType { NONE, SOMETIMES, OFTEN }
```

- [ ] **Step 2: Member Entity 작성**

```kotlin
// backend/src/main/kotlin/com/blinddate/member/entity/Member.kt
package com.blinddate.member.entity

import com.blinddate.common.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "member")
class Member(
    @Column(unique = true, nullable = false)
    val kakaoId: String,

    @Column
    var email: String = "",

    @Column
    var nickname: String = "",

    @Column
    var phoneNumber: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role = Role.USER
) : BaseEntity()
```

- [ ] **Step 3: MemberProfile Entity 작성**

```kotlin
// backend/src/main/kotlin/com/blinddate/member/entity/MemberProfile.kt
package com.blinddate.member.entity

import com.blinddate.common.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "member_profile")
class MemberProfile(
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", unique = true, nullable = false)
    val member: Member,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var age: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var gender: Gender,

    @Column(nullable = false)
    var job: String,

    @Column
    var height: Int = 0,

    @Column(length = 4)
    var mbti: String = "",

    @Column(columnDefinition = "TEXT")
    var hobby: String = "",

    @Enumerated(EnumType.STRING)
    var drinking: DrinkingType = DrinkingType.NONE,

    @Enumerated(EnumType.STRING)
    var smoking: SmokingType = SmokingType.NONE,

    @Column
    var religion: String = "",

    @Column(columnDefinition = "TEXT")
    var idealType: String = "",

    @Column(columnDefinition = "TEXT")
    var introduction: String = "",

    @Column
    var photoUrl: String = ""
) : BaseEntity()
```

- [ ] **Step 4: Repository 작성**

```kotlin
// backend/src/main/kotlin/com/blinddate/member/repository/MemberRepository.kt
package com.blinddate.member.repository

import com.blinddate.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface MemberRepository : JpaRepository<Member, Long> {
    fun findByKakaoId(kakaoId: String): Optional<Member>
}

// backend/src/main/kotlin/com/blinddate/member/repository/MemberProfileRepository.kt
package com.blinddate.member.repository

import com.blinddate.member.entity.MemberProfile
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface MemberProfileRepository : JpaRepository<MemberProfile, Long> {
    fun findByMemberId(memberId: Long): Optional<MemberProfile>
    fun existsByMemberId(memberId: Long): Boolean
}
```

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/kotlin/com/blinddate/member/
git commit -m "feat: add Member, MemberProfile entities and repositories"
```

### Task 5: JWT Token Provider

**Files:**
- Create: `backend/src/main/kotlin/com/blinddate/auth/jwt/JwtTokenProvider.kt`
- Test: `backend/src/test/kotlin/com/blinddate/auth/jwt/JwtTokenProviderTest.kt`

- [ ] **Step 1: 테스트 작성**

```kotlin
// backend/src/test/kotlin/com/blinddate/auth/jwt/JwtTokenProviderTest.kt
package com.blinddate.auth.jwt

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class JwtTokenProviderTest {

    private lateinit var provider: JwtTokenProvider

    @BeforeEach
    fun setUp() {
        provider = JwtTokenProvider(
            secret = "test-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm",
            accessTokenExpiry = 1800000,
            refreshTokenExpiry = 1209600000
        )
    }

    @Test
    fun `should create and validate access token`() {
        val token = provider.createAccessToken(1L, "USER")
        assertTrue(provider.validateToken(token))
        assertEquals(1L, provider.getMemberId(token))
        assertEquals("USER", provider.getRole(token))
    }

    @Test
    fun `should create refresh token`() {
        val token = provider.createRefreshToken(1L)
        assertTrue(provider.validateToken(token))
        assertEquals(1L, provider.getMemberId(token))
    }

    @Test
    fun `should fail validation for invalid token`() {
        assertFalse(provider.validateToken("invalid.token.here"))
    }
}
```

- [ ] **Step 2: 테스트 실행 - 실패 확인**

```bash
cd backend && ./gradlew test --tests "com.blinddate.auth.jwt.JwtTokenProviderTest"
```

Expected: FAIL (class not found)

- [ ] **Step 3: JwtTokenProvider 구현**

```kotlin
// backend/src/main/kotlin/com/blinddate/auth/jwt/JwtTokenProvider.kt
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
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    fun createAccessToken(memberId: Long, role: String): String {
        return createToken(memberId, role, accessTokenExpiry)
    }

    fun createRefreshToken(memberId: Long): String {
        return createToken(memberId, null, refreshTokenExpiry)
    }

    private fun createToken(memberId: Long, role: String?, expiry: Long): String {
        val now = Date()
        val builder = Jwts.builder()
            .subject(memberId.toString())
            .issuedAt(now)
            .expiration(Date(now.time + expiry))
        if (role != null) {
            builder.claim("role", role)
        }
        return builder.signWith(key).compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getMemberId(token: String): Long {
        return Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token).payload.subject.toLong()
    }

    fun getRole(token: String): String? {
        return Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token).payload["role"] as? String
    }

    fun getExpiration(token: String): Date {
        return Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token).payload.expiration
    }
}
```

- [ ] **Step 4: 테스트 실행 - 성공 확인**

```bash
cd backend && ./gradlew test --tests "com.blinddate.auth.jwt.JwtTokenProviderTest"
```

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add backend/src/
git commit -m "feat: add JWT token provider with tests"
```

### Task 6: JWT Authentication Filter + Security Config

**Files:**
- Create: `backend/src/main/kotlin/com/blinddate/auth/jwt/JwtAuthenticationFilter.kt`
- Create: `backend/src/main/kotlin/com/blinddate/common/config/WebSecurityConfig.kt`

- [ ] **Step 1: JwtAuthenticationFilter 작성**

```kotlin
// backend/src/main/kotlin/com/blinddate/auth/jwt/JwtAuthenticationFilter.kt
package com.blinddate.auth.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val redisTemplate: RedisTemplate<String, String>
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = resolveToken(request)

        if (token != null && jwtTokenProvider.validateToken(token) && !isBlacklisted(token)) {
            val memberId = jwtTokenProvider.getMemberId(token)
            val role = jwtTokenProvider.getRole(token) ?: "USER"
            val authorities = listOf(SimpleGrantedAuthority("ROLE_$role"))
            val auth = UsernamePasswordAuthenticationToken(memberId, null, authorities)
            SecurityContextHolder.getContext().authentication = auth
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val bearer = request.getHeader("Authorization") ?: return null
        return if (bearer.startsWith("Bearer ")) bearer.substring(7) else null
    }

    private fun isBlacklisted(token: String): Boolean {
        return redisTemplate.hasKey("auth:blacklist:$token")
    }
}
```

- [ ] **Step 2: WebSecurityConfig 작성**

```kotlin
// backend/src/main/kotlin/com/blinddate/common/config/WebSecurityConfig.kt
package com.blinddate.common.config

import com.blinddate.auth.jwt.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class WebSecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/payments/webhook").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/events/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/bar/status").permitAll()
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            allowedOrigins = listOf("http://localhost:5173")
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE")
            allowedHeaders = listOf("*")
            allowCredentials = true
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add backend/src/
git commit -m "feat: add JWT filter and Spring Security config"
```

### Task 7: Kakao OAuth + Auth Service + Controller

**Files:**
- Create: `backend/src/main/kotlin/com/blinddate/auth/service/KakaoOAuthService.kt`
- Create: `backend/src/main/kotlin/com/blinddate/auth/service/AuthService.kt`
- Create: `backend/src/main/kotlin/com/blinddate/auth/dto/AuthDtos.kt`
- Create: `backend/src/main/kotlin/com/blinddate/auth/controller/AuthController.kt`
- Create: `backend/src/main/kotlin/com/blinddate/auth/repository/RefreshTokenRepository.kt`
- Test: `backend/src/test/kotlin/com/blinddate/auth/service/AuthServiceTest.kt`

- [ ] **Step 1: DTO 작성**

```kotlin
// backend/src/main/kotlin/com/blinddate/auth/dto/AuthDtos.kt
package com.blinddate.auth.dto

data class KakaoLoginRequest(val code: String)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val isNewMember: Boolean
)

data class KakaoUserInfo(
    val id: String,
    val email: String,
    val nickname: String
)
```

- [ ] **Step 2: RefreshTokenRepository (Redis)**

```kotlin
// backend/src/main/kotlin/com/blinddate/auth/repository/RefreshTokenRepository.kt
package com.blinddate.auth.repository

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit

@Repository
class RefreshTokenRepository(
    private val redisTemplate: RedisTemplate<String, String>
) {
    private val prefix = "auth:refresh:"

    fun save(memberId: Long, refreshToken: String, expiryMs: Long) {
        redisTemplate.opsForValue().set(
            "$prefix$memberId", refreshToken, expiryMs, TimeUnit.MILLISECONDS
        )
    }

    fun find(memberId: Long): String? {
        return redisTemplate.opsForValue().get("$prefix$memberId")
    }

    fun delete(memberId: Long) {
        redisTemplate.delete("$prefix$memberId")
    }
}
```

- [ ] **Step 3: KakaoOAuthService 작성**

```kotlin
// backend/src/main/kotlin/com/blinddate/auth/service/KakaoOAuthService.kt
package com.blinddate.auth.service

import com.blinddate.auth.dto.KakaoUserInfo
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class KakaoOAuthService(
    @Value("\${kakao.client-id}") private val clientId: String,
    @Value("\${kakao.redirect-uri}") private val redirectUri: String
) {
    private val webClient = WebClient.create()

    fun getAccessToken(code: String): String {
        val response = webClient.post()
            .uri("https://kauth.kakao.com/oauth/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue("grant_type=authorization_code&client_id=$clientId&redirect_uri=$redirectUri&code=$code")
            .retrieve()
            .bodyToMono(Map::class.java)
            .block()!!

        return response["access_token"] as String
    }

    fun getUserInfo(accessToken: String): KakaoUserInfo {
        val response = webClient.get()
            .uri("https://kapi.kakao.com/v2/user/me")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .bodyToMono(Map::class.java)
            .block()!!

        val id = response["id"].toString()
        val kakaoAccount = response["kakao_account"] as? Map<*, *>
        val profile = kakaoAccount?.get("profile") as? Map<*, *>
        val email = kakaoAccount?.get("email") as? String ?: ""
        val nickname = profile?.get("nickname") as? String ?: ""

        return KakaoUserInfo(id = id, email = email, nickname = nickname)
    }
}
```

- [ ] **Step 4: AuthService 테스트 작성**

```kotlin
// backend/src/test/kotlin/com/blinddate/auth/service/AuthServiceTest.kt
package com.blinddate.auth.service

import com.blinddate.auth.dto.KakaoUserInfo
import com.blinddate.auth.jwt.JwtTokenProvider
import com.blinddate.auth.repository.RefreshTokenRepository
import com.blinddate.member.entity.Member
import com.blinddate.member.entity.Role
import com.blinddate.member.repository.MemberRepository
import com.blinddate.member.repository.MemberProfileRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional

class AuthServiceTest {

    private lateinit var authService: AuthService
    private val kakaoOAuthService = mockk<KakaoOAuthService>()
    private val memberRepository = mockk<MemberRepository>()
    private val memberProfileRepository = mockk<MemberProfileRepository>()
    private val jwtTokenProvider = mockk<JwtTokenProvider>()
    private val refreshTokenRepository = mockk<RefreshTokenRepository>(relaxed = true)

    @BeforeEach
    fun setUp() {
        authService = AuthService(
            kakaoOAuthService, memberRepository, memberProfileRepository,
            jwtTokenProvider, refreshTokenRepository
        )
    }

    @Test
    fun `should login existing member`() {
        val member = Member(kakaoId = "123", email = "test@test.com", nickname = "test").apply {
            val idField = com.blinddate.common.entity.BaseEntity::class.java.getDeclaredField("id")
            idField.isAccessible = true
            idField.set(this, 1L)
        }

        every { kakaoOAuthService.getAccessToken("code123") } returns "kakao-token"
        every { kakaoOAuthService.getUserInfo("kakao-token") } returns KakaoUserInfo("123", "test@test.com", "test")
        every { memberRepository.findByKakaoId("123") } returns Optional.of(member)
        every { memberProfileRepository.existsByMemberId(1L) } returns true
        every { jwtTokenProvider.createAccessToken(1L, "USER") } returns "access-token"
        every { jwtTokenProvider.createRefreshToken(1L) } returns "refresh-token"

        val result = authService.kakaoLogin("code123")

        assertEquals("access-token", result.accessToken)
        assertFalse(result.isNewMember)
    }

    @Test
    fun `should register new member on first login`() {
        every { kakaoOAuthService.getAccessToken("code123") } returns "kakao-token"
        every { kakaoOAuthService.getUserInfo("kakao-token") } returns KakaoUserInfo("999", "new@test.com", "newbie")
        every { memberRepository.findByKakaoId("999") } returns Optional.empty()
        every { memberRepository.save(any()) } answers {
            val m = firstArg<Member>()
            val idField = com.blinddate.common.entity.BaseEntity::class.java.getDeclaredField("id")
            idField.isAccessible = true
            idField.set(m, 2L)
            m
        }
        every { memberProfileRepository.existsByMemberId(2L) } returns false
        every { jwtTokenProvider.createAccessToken(2L, "USER") } returns "access-token-new"
        every { jwtTokenProvider.createRefreshToken(2L) } returns "refresh-token-new"

        val result = authService.kakaoLogin("code123")

        assertEquals("access-token-new", result.accessToken)
        assertTrue(result.isNewMember)
        verify { memberRepository.save(any()) }
    }
}
```

- [ ] **Step 5: 테스트 실행 - 실패 확인**

```bash
cd backend && ./gradlew test --tests "com.blinddate.auth.service.AuthServiceTest"
```

- [ ] **Step 6: AuthService 구현**

```kotlin
// backend/src/main/kotlin/com/blinddate/auth/service/AuthService.kt
package com.blinddate.auth.service

import com.blinddate.auth.dto.KakaoLoginRequest
import com.blinddate.auth.dto.TokenResponse
import com.blinddate.auth.jwt.JwtTokenProvider
import com.blinddate.auth.repository.RefreshTokenRepository
import com.blinddate.common.exception.UnauthorizedException
import com.blinddate.member.entity.Member
import com.blinddate.member.repository.MemberProfileRepository
import com.blinddate.member.repository.MemberRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class AuthService(
    private val kakaoOAuthService: KakaoOAuthService,
    private val memberRepository: MemberRepository,
    private val memberProfileRepository: MemberProfileRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val refreshTokenRepository: RefreshTokenRepository
) {
    fun kakaoLogin(code: String): TokenResponse {
        val kakaoAccessToken = kakaoOAuthService.getAccessToken(code)
        val userInfo = kakaoOAuthService.getUserInfo(kakaoAccessToken)

        val member = memberRepository.findByKakaoId(userInfo.id).orElseGet {
            memberRepository.save(
                Member(
                    kakaoId = userInfo.id,
                    email = userInfo.email,
                    nickname = userInfo.nickname
                )
            )
        }

        val hasProfile = memberProfileRepository.existsByMemberId(member.id)
        val accessToken = jwtTokenProvider.createAccessToken(member.id, member.role.name)
        val refreshToken = jwtTokenProvider.createRefreshToken(member.id)

        refreshTokenRepository.save(member.id, refreshToken, 1209600000)

        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            isNewMember = !hasProfile
        )
    }

    fun refresh(refreshToken: String): TokenResponse {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw UnauthorizedException("유효하지 않은 리프레시 토큰입니다")
        }

        val memberId = jwtTokenProvider.getMemberId(refreshToken)
        val stored = refreshTokenRepository.find(memberId)
            ?: throw UnauthorizedException("만료된 리프레시 토큰입니다")

        if (stored != refreshToken) {
            throw UnauthorizedException("리프레시 토큰이 일치하지 않습니다")
        }

        val member = memberRepository.findById(memberId)
            .orElseThrow { UnauthorizedException("존재하지 않는 회원입니다") }

        val newAccessToken = jwtTokenProvider.createAccessToken(member.id, member.role.name)
        val newRefreshToken = jwtTokenProvider.createRefreshToken(member.id)

        refreshTokenRepository.delete(memberId)
        refreshTokenRepository.save(memberId, newRefreshToken, 1209600000)

        return TokenResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
            isNewMember = false
        )
    }

    fun logout(memberId: Long, accessToken: String) {
        refreshTokenRepository.delete(memberId)
        val expiration = jwtTokenProvider.getExpiration(accessToken)
        val ttl = expiration.time - System.currentTimeMillis()
        if (ttl > 0) {
            // Blacklist handled by caller with RedisTemplate directly
        }
    }
}
```

- [ ] **Step 7: AuthController 작성**

```kotlin
// backend/src/main/kotlin/com/blinddate/auth/controller/AuthController.kt
package com.blinddate.auth.controller

import com.blinddate.auth.dto.KakaoLoginRequest
import com.blinddate.auth.dto.TokenResponse
import com.blinddate.auth.service.AuthService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/kakao/login")
    fun kakaoLogin(@RequestBody request: KakaoLoginRequest): ResponseEntity<TokenResponse> {
        val response = authService.kakaoLogin(request.code)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/refresh")
    fun refresh(@RequestBody body: Map<String, String>): ResponseEntity<TokenResponse> {
        val refreshToken = body["refreshToken"]
            ?: return ResponseEntity.badRequest().build()
        return ResponseEntity.ok(authService.refresh(refreshToken))
    }

    @DeleteMapping("/logout")
    fun logout(request: HttpServletRequest): ResponseEntity<Void> {
        val memberId = SecurityContextHolder.getContext().authentication.principal as Long
        val token = request.getHeader("Authorization")?.substring(7) ?: ""
        authService.logout(memberId, token)
        return ResponseEntity.noContent().build()
    }
}
```

- [ ] **Step 8: 테스트 실행 - 성공 확인**

```bash
cd backend && ./gradlew test --tests "com.blinddate.auth.service.AuthServiceTest"
```

- [ ] **Step 9: Commit**

```bash
git add backend/src/
git commit -m "feat: add Kakao OAuth login, auth service, JWT auth flow"
```

---

## Chunk 3: Member 프로필 + Event 모듈

### Task 8: Member Service + Controller

**Files:**
- Create: `backend/src/main/kotlin/com/blinddate/member/dto/MemberDtos.kt`
- Create: `backend/src/main/kotlin/com/blinddate/member/service/MemberService.kt`
- Create: `backend/src/main/kotlin/com/blinddate/member/controller/MemberController.kt`
- Test: `backend/src/test/kotlin/com/blinddate/member/service/MemberServiceTest.kt`

- [ ] **Step 1: DTO 작성**

```kotlin
// backend/src/main/kotlin/com/blinddate/member/dto/MemberDtos.kt
package com.blinddate.member.dto

import com.blinddate.member.entity.*
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class MemberResponse(
    val id: Long,
    val email: String,
    val nickname: String,
    val phoneNumber: String,
    val role: Role,
    val hasProfile: Boolean
)

data class ProfileCreateRequest(
    @field:NotBlank val name: String,
    @field:Min(18) val age: Int,
    val gender: Gender,
    @field:NotBlank val job: String,
    val height: Int = 0,
    val mbti: String = "",
    val hobby: String = "",
    val drinking: DrinkingType = DrinkingType.NONE,
    val smoking: SmokingType = SmokingType.NONE,
    val religion: String = "",
    val idealType: String = "",
    val introduction: String = ""
)

data class ProfileResponse(
    val id: Long,
    val name: String,
    val age: Int,
    val gender: Gender,
    val job: String,
    val height: Int,
    val mbti: String,
    val hobby: String,
    val drinking: DrinkingType,
    val smoking: SmokingType,
    val religion: String,
    val idealType: String,
    val introduction: String,
    val photoUrl: String
)
```

- [ ] **Step 2: MemberService 테스트 작성**

```kotlin
// backend/src/test/kotlin/com/blinddate/member/service/MemberServiceTest.kt
package com.blinddate.member.service

import com.blinddate.common.exception.ConflictException
import com.blinddate.common.exception.NotFoundException
import com.blinddate.member.dto.ProfileCreateRequest
import com.blinddate.member.entity.*
import com.blinddate.member.repository.MemberProfileRepository
import com.blinddate.member.repository.MemberRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional

class MemberServiceTest {

    private lateinit var memberService: MemberService
    private val memberRepository = mockk<MemberRepository>()
    private val profileRepository = mockk<MemberProfileRepository>()

    @BeforeEach
    fun setUp() {
        memberService = MemberService(memberRepository, profileRepository)
    }

    @Test
    fun `should create profile`() {
        val member = Member(kakaoId = "123").apply {
            val f = com.blinddate.common.entity.BaseEntity::class.java.getDeclaredField("id")
            f.isAccessible = true; f.set(this, 1L)
        }
        every { memberRepository.findById(1L) } returns Optional.of(member)
        every { profileRepository.existsByMemberId(1L) } returns false
        every { profileRepository.save(any()) } answers { firstArg() }

        val request = ProfileCreateRequest(
            name = "테스트", age = 25, gender = Gender.MALE, job = "개발자"
        )
        val result = memberService.createProfile(1L, request)

        assertEquals("테스트", result.name)
        verify { profileRepository.save(any()) }
    }

    @Test
    fun `should throw if profile already exists`() {
        val member = Member(kakaoId = "123").apply {
            val f = com.blinddate.common.entity.BaseEntity::class.java.getDeclaredField("id")
            f.isAccessible = true; f.set(this, 1L)
        }
        every { memberRepository.findById(1L) } returns Optional.of(member)
        every { profileRepository.existsByMemberId(1L) } returns true

        val request = ProfileCreateRequest(
            name = "테스트", age = 25, gender = Gender.MALE, job = "개발자"
        )
        assertThrows<ConflictException> { memberService.createProfile(1L, request) }
    }
}
```

- [ ] **Step 3: 테스트 실행 - 실패 확인**

```bash
cd backend && ./gradlew test --tests "com.blinddate.member.service.MemberServiceTest"
```

- [ ] **Step 4: MemberService 구현**

```kotlin
// backend/src/main/kotlin/com/blinddate/member/service/MemberService.kt
package com.blinddate.member.service

import com.blinddate.common.exception.ConflictException
import com.blinddate.common.exception.NotFoundException
import com.blinddate.member.dto.*
import com.blinddate.member.entity.MemberProfile
import com.blinddate.member.repository.MemberProfileRepository
import com.blinddate.member.repository.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class MemberService(
    private val memberRepository: MemberRepository,
    private val profileRepository: MemberProfileRepository
) {
    fun getMe(memberId: Long): MemberResponse {
        val member = memberRepository.findById(memberId)
            .orElseThrow { NotFoundException("회원을 찾을 수 없습니다") }
        val hasProfile = profileRepository.existsByMemberId(memberId)
        return MemberResponse(
            id = member.id, email = member.email, nickname = member.nickname,
            phoneNumber = member.phoneNumber, role = member.role, hasProfile = hasProfile
        )
    }

    @Transactional
    fun createProfile(memberId: Long, request: ProfileCreateRequest): ProfileResponse {
        val member = memberRepository.findById(memberId)
            .orElseThrow { NotFoundException("회원을 찾을 수 없습니다") }
        if (profileRepository.existsByMemberId(memberId)) {
            throw ConflictException("프로필이 이미 존재합니다")
        }
        val profile = profileRepository.save(
            MemberProfile(
                member = member, name = request.name, age = request.age,
                gender = request.gender, job = request.job, height = request.height,
                mbti = request.mbti, hobby = request.hobby, drinking = request.drinking,
                smoking = request.smoking, religion = request.religion,
                idealType = request.idealType, introduction = request.introduction
            )
        )
        return profile.toResponse()
    }

    fun getProfile(memberId: Long): ProfileResponse {
        val profile = profileRepository.findByMemberId(memberId)
            .orElseThrow { NotFoundException("프로필을 찾을 수 없습니다") }
        return profile.toResponse()
    }

    @Transactional
    fun updateProfile(memberId: Long, request: ProfileCreateRequest): ProfileResponse {
        val profile = profileRepository.findByMemberId(memberId)
            .orElseThrow { NotFoundException("프로필을 찾을 수 없습니다") }
        profile.apply {
            name = request.name; age = request.age; gender = request.gender
            job = request.job; height = request.height; mbti = request.mbti
            hobby = request.hobby; drinking = request.drinking; smoking = request.smoking
            religion = request.religion; idealType = request.idealType
            introduction = request.introduction
        }
        return profile.toResponse()
    }

    private fun MemberProfile.toResponse() = ProfileResponse(
        id = id, name = name, age = age, gender = gender, job = job,
        height = height, mbti = mbti, hobby = hobby, drinking = drinking,
        smoking = smoking, religion = religion, idealType = idealType,
        introduction = introduction, photoUrl = photoUrl
    )
}
```

- [ ] **Step 5: MemberController 작성**

```kotlin
// backend/src/main/kotlin/com/blinddate/member/controller/MemberController.kt
package com.blinddate.member.controller

import com.blinddate.member.dto.*
import com.blinddate.member.service.MemberService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/members")
class MemberController(
    private val memberService: MemberService
) {
    private fun currentMemberId(): Long =
        SecurityContextHolder.getContext().authentication.principal as Long

    @GetMapping("/me")
    fun getMe(): ResponseEntity<MemberResponse> =
        ResponseEntity.ok(memberService.getMe(currentMemberId()))

    @PostMapping("/me/profile")
    fun createProfile(@Valid @RequestBody request: ProfileCreateRequest): ResponseEntity<ProfileResponse> =
        ResponseEntity.ok(memberService.createProfile(currentMemberId(), request))

    @GetMapping("/me/profile")
    fun getProfile(): ResponseEntity<ProfileResponse> =
        ResponseEntity.ok(memberService.getProfile(currentMemberId()))

    @PutMapping("/me/profile")
    fun updateProfile(@Valid @RequestBody request: ProfileCreateRequest): ResponseEntity<ProfileResponse> =
        ResponseEntity.ok(memberService.updateProfile(currentMemberId(), request))
}
```

- [ ] **Step 6: 테스트 실행 - 성공 확인**

```bash
cd backend && ./gradlew test --tests "com.blinddate.member.service.MemberServiceTest"
```

- [ ] **Step 7: Commit**

```bash
git add backend/src/
git commit -m "feat: add member profile CRUD service and controller"
```

### Task 9: Event Entity + Service + Controller

**Files:**
- Create: `backend/src/main/kotlin/com/blinddate/event/entity/EventStatus.kt`
- Create: `backend/src/main/kotlin/com/blinddate/event/entity/BlindDateEvent.kt`
- Create: `backend/src/main/kotlin/com/blinddate/event/repository/BlindDateEventRepository.kt`
- Create: `backend/src/main/kotlin/com/blinddate/event/dto/EventDtos.kt`
- Create: `backend/src/main/kotlin/com/blinddate/event/service/EventService.kt`
- Create: `backend/src/main/kotlin/com/blinddate/event/controller/EventController.kt`
- Create: `backend/src/main/kotlin/com/blinddate/event/controller/AdminEventController.kt`
- Test: `backend/src/test/kotlin/com/blinddate/event/service/EventServiceTest.kt`

- [ ] **Step 1: Entity + Enum + Repository + DTO 작성**

```kotlin
// EventStatus.kt
package com.blinddate.event.entity
enum class EventStatus { OPEN, CLOSED, COMPLETED }
```

```kotlin
// BlindDateEvent.kt
package com.blinddate.event.entity

import com.blinddate.common.entity.BaseEntity
import com.blinddate.member.entity.Member
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity
@Table(name = "blind_date_event")
class BlindDateEvent(
    @Column(nullable = false) var title: String,
    @Column(nullable = false) var date: LocalDate,
    @Column(nullable = false) var time: LocalTime,
    @Column(nullable = false) var maleCapacity: Int,
    @Column(nullable = false) var femaleCapacity: Int,
    @Column(nullable = false) var currentMaleCount: Int = 0,
    @Column(nullable = false) var currentFemaleCount: Int = 0,
    @Column(nullable = false) var price: Int,
    @Enumerated(EnumType.STRING) var status: EventStatus = EventStatus.OPEN,
    @Column(columnDefinition = "TEXT") var description: String = "",
    var choiceDeadline: LocalDateTime? = null,
    var matchNotificationTime: LocalDateTime? = null,
    var minAge: Int? = null,
    var maxAge: Int? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by") val createdBy: Member,
    var deletedAt: LocalDateTime? = null
) : BaseEntity() {
    fun isDeleted() = deletedAt != null
    fun hasAvailableMaleSlots() = currentMaleCount < maleCapacity
    fun hasAvailableFemaleSlots() = currentFemaleCount < femaleCapacity
}
```

```kotlin
// BlindDateEventRepository.kt
package com.blinddate.event.repository

import com.blinddate.event.entity.BlindDateEvent
import com.blinddate.event.entity.EventStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface BlindDateEventRepository : JpaRepository<BlindDateEvent, Long> {
    @Query("SELECT e FROM BlindDateEvent e WHERE e.deletedAt IS NULL AND YEAR(e.date) = :year AND MONTH(e.date) = :month ORDER BY e.date, e.time")
    fun findByYearAndMonth(year: Int, month: Int): List<BlindDateEvent>

    @Query("SELECT e FROM BlindDateEvent e WHERE e.deletedAt IS NULL AND e.status = :status")
    fun findByStatus(status: EventStatus): List<BlindDateEvent>
}
```

```kotlin
// EventDtos.kt
package com.blinddate.event.dto

import com.blinddate.event.entity.EventStatus
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class EventCreateRequest(
    @field:NotBlank val title: String,
    val date: LocalDate,
    val time: LocalTime,
    @field:Min(1) val maleCapacity: Int,
    @field:Min(1) val femaleCapacity: Int,
    @field:Min(0) val price: Int,
    val description: String = "",
    val choiceDeadline: LocalDateTime? = null,
    val matchNotificationTime: LocalDateTime? = null,
    val minAge: Int? = null,
    val maxAge: Int? = null
)

data class EventResponse(
    val id: Long,
    val title: String,
    val date: LocalDate,
    val time: LocalTime,
    val maleCapacity: Int,
    val femaleCapacity: Int,
    val currentMaleCount: Int,
    val currentFemaleCount: Int,
    val price: Int,
    val status: EventStatus,
    val description: String,
    val choiceDeadline: LocalDateTime?,
    val matchNotificationTime: LocalDateTime?,
    val minAge: Int?,
    val maxAge: Int?
)
```

- [ ] **Step 2: EventService 테스트 작성**

```kotlin
// backend/src/test/kotlin/com/blinddate/event/service/EventServiceTest.kt
package com.blinddate.event.service

import com.blinddate.event.dto.EventCreateRequest
import com.blinddate.event.entity.BlindDateEvent
import com.blinddate.event.entity.EventStatus
import com.blinddate.event.repository.BlindDateEventRepository
import com.blinddate.member.entity.Member
import com.blinddate.member.repository.MemberRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import java.util.Optional

class EventServiceTest {

    private lateinit var eventService: EventService
    private val eventRepository = mockk<BlindDateEventRepository>()
    private val memberRepository = mockk<MemberRepository>()

    @BeforeEach
    fun setUp() {
        eventService = EventService(eventRepository, memberRepository)
    }

    @Test
    fun `should list events by month`() {
        every { eventRepository.findByYearAndMonth(2026, 3) } returns emptyList()
        val result = eventService.getEventsByMonth(2026, 3)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should create event`() {
        val admin = Member(kakaoId = "admin").apply {
            val f = com.blinddate.common.entity.BaseEntity::class.java.getDeclaredField("id")
            f.isAccessible = true; f.set(this, 1L)
        }
        every { memberRepository.findById(1L) } returns Optional.of(admin)
        every { eventRepository.save(any()) } answers { firstArg() }

        val request = EventCreateRequest(
            title = "테스트 이벤트", date = LocalDate.of(2026, 3, 21),
            time = LocalTime.of(19, 0), maleCapacity = 3, femaleCapacity = 3, price = 30000
        )
        val result = eventService.createEvent(1L, request)

        assertEquals("테스트 이벤트", result.title)
        assertEquals(EventStatus.OPEN, result.status)
    }
}
```

- [ ] **Step 3: 테스트 실행 - 실패 확인**

```bash
cd backend && ./gradlew test --tests "com.blinddate.event.service.EventServiceTest"
```

- [ ] **Step 4: EventService 구현**

```kotlin
// backend/src/main/kotlin/com/blinddate/event/service/EventService.kt
package com.blinddate.event.service

import com.blinddate.common.exception.BadRequestException
import com.blinddate.common.exception.NotFoundException
import com.blinddate.event.dto.*
import com.blinddate.event.entity.BlindDateEvent
import com.blinddate.event.repository.BlindDateEventRepository
import com.blinddate.member.repository.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class EventService(
    private val eventRepository: BlindDateEventRepository,
    private val memberRepository: MemberRepository
) {
    fun getEventsByMonth(year: Int, month: Int): List<EventResponse> {
        return eventRepository.findByYearAndMonth(year, month).map { it.toResponse() }
    }

    fun getEvent(eventId: Long): EventResponse {
        val event = eventRepository.findById(eventId)
            .orElseThrow { NotFoundException("이벤트를 찾을 수 없습니다") }
        if (event.isDeleted()) throw NotFoundException("삭제된 이벤트입니다")
        return event.toResponse()
    }

    @Transactional
    fun createEvent(adminId: Long, request: EventCreateRequest): EventResponse {
        val admin = memberRepository.findById(adminId)
            .orElseThrow { NotFoundException("관리자를 찾을 수 없습니다") }
        val event = eventRepository.save(
            BlindDateEvent(
                title = request.title, date = request.date, time = request.time,
                maleCapacity = request.maleCapacity, femaleCapacity = request.femaleCapacity,
                price = request.price, description = request.description,
                choiceDeadline = request.choiceDeadline,
                matchNotificationTime = request.matchNotificationTime,
                minAge = request.minAge, maxAge = request.maxAge, createdBy = admin
            )
        )
        return event.toResponse()
    }

    @Transactional
    fun updateEvent(eventId: Long, request: EventCreateRequest): EventResponse {
        val event = eventRepository.findById(eventId)
            .orElseThrow { NotFoundException("이벤트를 찾을 수 없습니다") }
        event.apply {
            title = request.title; date = request.date; time = request.time
            maleCapacity = request.maleCapacity; femaleCapacity = request.femaleCapacity
            price = request.price; description = request.description
            choiceDeadline = request.choiceDeadline
            matchNotificationTime = request.matchNotificationTime
            minAge = request.minAge; maxAge = request.maxAge
        }
        return event.toResponse()
    }

    @Transactional
    fun deleteEvent(eventId: Long) {
        val event = eventRepository.findById(eventId)
            .orElseThrow { NotFoundException("이벤트를 찾을 수 없습니다") }
        event.deletedAt = LocalDateTime.now()
    }

    private fun BlindDateEvent.toResponse() = EventResponse(
        id = id, title = title, date = date, time = time,
        maleCapacity = maleCapacity, femaleCapacity = femaleCapacity,
        currentMaleCount = currentMaleCount, currentFemaleCount = currentFemaleCount,
        price = price, status = status, description = description,
        choiceDeadline = choiceDeadline, matchNotificationTime = matchNotificationTime,
        minAge = minAge, maxAge = maxAge
    )
}
```

- [ ] **Step 5: Controller 작성**

```kotlin
// EventController.kt
package com.blinddate.event.controller

import com.blinddate.event.dto.EventResponse
import com.blinddate.event.service.EventService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/events")
class EventController(private val eventService: EventService) {

    @GetMapping
    fun getEvents(@RequestParam year: Int, @RequestParam month: Int): ResponseEntity<List<EventResponse>> =
        ResponseEntity.ok(eventService.getEventsByMonth(year, month))

    @GetMapping("/{id}")
    fun getEvent(@PathVariable id: Long): ResponseEntity<EventResponse> =
        ResponseEntity.ok(eventService.getEvent(id))
}

// AdminEventController.kt
package com.blinddate.event.controller

import com.blinddate.event.dto.*
import com.blinddate.event.service.EventService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/events")
class AdminEventController(private val eventService: EventService) {

    private fun currentMemberId(): Long =
        SecurityContextHolder.getContext().authentication.principal as Long

    @PostMapping
    fun create(@Valid @RequestBody request: EventCreateRequest): ResponseEntity<EventResponse> =
        ResponseEntity.ok(eventService.createEvent(currentMemberId(), request))

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: EventCreateRequest): ResponseEntity<EventResponse> =
        ResponseEntity.ok(eventService.updateEvent(id, request))

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        eventService.deleteEvent(id)
        return ResponseEntity.noContent().build()
    }
}
```

- [ ] **Step 6: 테스트 실행 - 성공 확인**

```bash
cd backend && ./gradlew test --tests "com.blinddate.event.service.EventServiceTest"
```

- [ ] **Step 7: Commit**

```bash
git add backend/src/
git commit -m "feat: add blind date event CRUD with calendar query"
```

---

## Chunk 4: Application + Payment 모듈

### Task 10: Application Entity + Service

**Files:**
- Create: `backend/src/main/kotlin/com/blinddate/application/entity/ApplicationStatus.kt`
- Create: `backend/src/main/kotlin/com/blinddate/application/entity/Application.kt`
- Create: `backend/src/main/kotlin/com/blinddate/application/repository/ApplicationRepository.kt`
- Create: `backend/src/main/kotlin/com/blinddate/application/dto/ApplicationDtos.kt`
- Create: `backend/src/main/kotlin/com/blinddate/application/service/ApplicationService.kt`
- Create: `backend/src/main/kotlin/com/blinddate/application/controller/ApplicationController.kt`
- Create: `backend/src/main/kotlin/com/blinddate/application/controller/AdminApplicationController.kt`
- Test: `backend/src/test/kotlin/com/blinddate/application/service/ApplicationServiceTest.kt`

- [ ] **Step 1: Entity + Enum + Repository + DTO 모두 작성** (이전 Task와 동일 패턴)
- [ ] **Step 2: ApplicationService 테스트 작성** — 신청, 중복 신청 방지, 나이 검증, 승인(비관적 락), 거절 케이스
- [ ] **Step 3: 테스트 실행 - 실패 확인**
- [ ] **Step 4: ApplicationService 구현** — `apply()`, `cancel()`, `approve()` (SELECT FOR UPDATE), `reject()`
- [ ] **Step 5: Controller 작성**
- [ ] **Step 6: 테스트 실행 - 성공 확인**
- [ ] **Step 7: Commit**

```bash
git commit -m "feat: add application module - apply, approve, reject with pessimistic locking"
```

### Task 11: Payment Entity + TossPayments 연동

**Files:**
- Create: `backend/src/main/kotlin/com/blinddate/payment/entity/Payment.kt`
- Create: `backend/src/main/kotlin/com/blinddate/payment/entity/PaymentStatus.kt`
- Create: `backend/src/main/kotlin/com/blinddate/payment/repository/PaymentRepository.kt`
- Create: `backend/src/main/kotlin/com/blinddate/payment/dto/PaymentDtos.kt`
- Create: `backend/src/main/kotlin/com/blinddate/payment/service/TossPaymentsClient.kt`
- Create: `backend/src/main/kotlin/com/blinddate/payment/service/PaymentService.kt`
- Create: `backend/src/main/kotlin/com/blinddate/payment/controller/PaymentController.kt`
- Test: `backend/src/test/kotlin/com/blinddate/payment/service/PaymentServiceTest.kt`

- [ ] **Step 1: Entity + DTO + TossPaymentsClient 작성**
- [ ] **Step 2: PaymentService 테스트** — confirm, refund, webhook 처리
- [ ] **Step 3: 테스트 실행 - 실패 확인**
- [ ] **Step 4: PaymentService 구현** — `confirm()` (Application 상태 PAID 전환), `refund()`, `handleWebhook()`
- [ ] **Step 5: PaymentController 작성** — `/api/payments/confirm`, `/api/payments/webhook`
- [ ] **Step 6: 테스트 실행 - 성공 확인**
- [ ] **Step 7: Commit**

```bash
git commit -m "feat: add payment module with Toss Payments integration"
```

---

## Chunk 5: Matching + Notification 모듈

### Task 12: Matching (번호 부여 + 선택 + 매칭)

**Files:**
- Create: `backend/src/main/kotlin/com/blinddate/matching/entity/ParticipantNumber.kt`
- Create: `backend/src/main/kotlin/com/blinddate/matching/entity/Choice.kt`
- Create: `backend/src/main/kotlin/com/blinddate/matching/entity/MatchResult.kt`
- Create: `backend/src/main/kotlin/com/blinddate/matching/repository/*.kt`
- Create: `backend/src/main/kotlin/com/blinddate/matching/dto/MatchingDtos.kt`
- Create: `backend/src/main/kotlin/com/blinddate/matching/service/ParticipantNumberService.kt`
- Create: `backend/src/main/kotlin/com/blinddate/matching/service/MatchingService.kt`
- Create: `backend/src/main/kotlin/com/blinddate/matching/controller/MatchingController.kt`
- Test: `backend/src/test/kotlin/com/blinddate/matching/service/MatchingServiceTest.kt`

- [ ] **Step 1: Entity + Repository + DTO 작성**
- [ ] **Step 2: 테스트 작성** — 번호 부여 (남/여 별도), 선택 (최대 3명), 양방향 매칭 검출
- [ ] **Step 3: 테스트 실행 - 실패 확인**
- [ ] **Step 4: ParticipantNumberService 구현** — 승인 시 호출, 성별별 다음 번호 자동 부여
- [ ] **Step 5: MatchingService 구현** — `submitChoices()` (최대 3명, 이성만), `processMatching()` (양방향 검출)
- [ ] **Step 6: MatchingController 작성**
- [ ] **Step 7: 테스트 실행 - 성공 확인**
- [ ] **Step 8: Commit**

```bash
git commit -m "feat: add matching module - participant numbers, choices, bidirectional matching"
```

### Task 13: Notification (앱 내 + 카카오 알림톡 + 웹훅 + 큐)

**Files:**
- Create: `backend/src/main/kotlin/com/blinddate/notification/entity/Notification.kt`
- Create: `backend/src/main/kotlin/com/blinddate/notification/entity/NotificationType.kt`
- Create: `backend/src/main/kotlin/com/blinddate/notification/repository/NotificationRepository.kt`
- Create: `backend/src/main/kotlin/com/blinddate/notification/dto/NotificationDtos.kt`
- Create: `backend/src/main/kotlin/com/blinddate/notification/service/NotificationService.kt`
- Create: `backend/src/main/kotlin/com/blinddate/notification/service/KakaoAlimtalkService.kt`
- Create: `backend/src/main/kotlin/com/blinddate/notification/service/WebhookService.kt`
- Create: `backend/src/main/kotlin/com/blinddate/notification/service/NotificationQueueConsumer.kt`
- Create: `backend/src/main/kotlin/com/blinddate/notification/controller/NotificationController.kt`
- Test: `backend/src/test/kotlin/com/blinddate/notification/service/NotificationServiceTest.kt`

- [ ] **Step 1: Entity + Repository + DTO 작성**
- [ ] **Step 2: 테스트 작성** — 알림 생성, 읽음 처리, 안읽은 수 카운트
- [ ] **Step 3: 테스트 실행 - 실패 확인**
- [ ] **Step 4: NotificationService 구현** — `send()` (Redis 큐에 push), `markAsRead()`, `getUnreadCount()`
- [ ] **Step 5: KakaoAlimtalkService 구현** — 카카오 비즈메시지 API 호출
- [ ] **Step 6: WebhookService 구현** — 외부 웹훅 (Slack 등) HTTP 호출
- [ ] **Step 7: NotificationQueueConsumer 구현** — `@Scheduled` BRPOP, 재시도/DLQ
- [ ] **Step 8: NotificationController 작성**
- [ ] **Step 9: 테스트 실행 - 성공 확인**
- [ ] **Step 10: Commit**

```bash
git commit -m "feat: add notification module with Kakao Alimtalk, webhook, Redis queue"
```

### Task 14: Schedulers (매칭, 리마인더, 상태 전환)

**Files:**
- Create: `backend/src/main/kotlin/com/blinddate/scheduler/MatchingScheduler.kt`
- Create: `backend/src/main/kotlin/com/blinddate/scheduler/EventStatusScheduler.kt`
- Create: `backend/src/main/kotlin/com/blinddate/scheduler/ReminderScheduler.kt`
- Test: `backend/src/test/kotlin/com/blinddate/scheduler/MatchingSchedulerTest.kt`

- [ ] **Step 1: 테스트 작성** — choice_deadline 지난 이벤트에서 매칭 실행 검증
- [ ] **Step 2: 테스트 실행 - 실패 확인**
- [ ] **Step 3: MatchingScheduler 구현** — 매분 실행, deadline 지난 미처리 이벤트 매칭
- [ ] **Step 4: EventStatusScheduler 구현** — 매일 자정, 날짜 지난 이벤트 COMPLETED 전환
- [ ] **Step 5: ReminderScheduler 구현** — 매일 오전 10시, D-1 리마인더
- [ ] **Step 6: 테스트 실행 - 성공 확인**
- [ ] **Step 7: Commit**

```bash
git commit -m "feat: add schedulers for matching, event status, reminders"
```

---

## Chunk 6: Bar (혼술바) 모듈

### Task 15: Bar Entity + Service + Controller

**Files:**
- Create: `backend/src/main/kotlin/com/blinddate/bar/entity/Bar.kt`
- Create: `backend/src/main/kotlin/com/blinddate/bar/entity/BarReservation.kt`
- Create: `backend/src/main/kotlin/com/blinddate/bar/entity/BarReservationStatus.kt`
- Create: `backend/src/main/kotlin/com/blinddate/bar/entity/BarVisitLog.kt`
- Create: `backend/src/main/kotlin/com/blinddate/bar/repository/*.kt`
- Create: `backend/src/main/kotlin/com/blinddate/bar/dto/BarDtos.kt`
- Create: `backend/src/main/kotlin/com/blinddate/bar/service/BarService.kt`
- Create: `backend/src/main/kotlin/com/blinddate/bar/service/BarStatusRedisService.kt`
- Create: `backend/src/main/kotlin/com/blinddate/bar/controller/BarController.kt`
- Create: `backend/src/main/kotlin/com/blinddate/bar/controller/AdminBarController.kt`
- Test: `backend/src/test/kotlin/com/blinddate/bar/service/BarServiceTest.kt`

- [ ] **Step 1: Entity + Enum + Repository + DTO 작성**
- [ ] **Step 2: BarStatusRedisService 테스트** — 입장 시 증가, 퇴장 시 감소, 잔여석 계산
- [ ] **Step 3: 테스트 실행 - 실패 확인**
- [ ] **Step 4: BarStatusRedisService 구현** — Redis Hash HINCRBY 원자적 증감
- [ ] **Step 5: BarService 구현** — `getStatus()`, `reserve()`, `checkIn()`, `checkOut()`, `openBar()`, `closeBar()`
- [ ] **Step 6: Controller 작성** — BarController (일반), AdminBarController (관리자)
- [ ] **Step 7: 테스트 실행 - 성공 확인**
- [ ] **Step 8: Commit**

```bash
git commit -m "feat: add bar module with real-time status via Redis"
```

---

## Chunk 7: Frontend 핵심 페이지

### Task 16: API Client + Auth Store + 공통 컴포넌트

**Files:**
- Create: `frontend/src/api/client.ts`
- Create: `frontend/src/stores/authStore.ts`
- Create: `frontend/src/components/layout/AppLayout.tsx`
- Create: `frontend/src/components/layout/BottomNav.tsx`
- Create: `frontend/src/components/layout/Header.tsx`
- Create: `frontend/src/components/auth/ProtectedRoute.tsx`
- Create: `frontend/src/components/common/Button.tsx`

- [ ] **Step 1: Axios 인스턴스 + 인터셉터** — access token 자동 첨부, 401 시 refresh
- [ ] **Step 2: Zustand auth store** — accessToken, member, login/logout actions
- [ ] **Step 3: AppLayout (Header + BottomNav + Outlet)**
- [ ] **Step 4: BottomNav** — 캘린더, 혼술바, 내 신청, 알림(배지), 마이, 관리(ADMIN)
- [ ] **Step 5: ProtectedRoute** — 미로그인 시 /login 리다이렉트
- [ ] **Step 6: Button 공통 컴포넌트** — 그라데이션 스타일
- [ ] **Step 7: Commit**

```bash
git commit -m "feat: add frontend API client, auth store, layout components"
```

### Task 17: Login + Kakao Callback + Profile Setup 페이지

**Files:**
- Create: `frontend/src/pages/LoginPage.tsx`
- Create: `frontend/src/pages/KakaoCallbackPage.tsx`
- Create: `frontend/src/pages/ProfileSetupPage.tsx`
- Create: `frontend/src/api/auth.ts`
- Create: `frontend/src/api/member.ts`

- [ ] **Step 1: LoginPage** — 핑크-오렌지 그라데이션 배경 + 카카오 로그인 버튼
- [ ] **Step 2: KakaoCallbackPage** — 인가코드 → /api/auth/kakao/login 호출 → 토큰 저장
- [ ] **Step 3: ProfileSetupPage** — 프로필 등록 폼 (이름, 나이, 성별, 직업, 키, MBTI 등)
- [ ] **Step 4: API 함수 작성** (auth.ts, member.ts)
- [ ] **Step 5: Commit**

```bash
git commit -m "feat: add login, kakao callback, profile setup pages"
```

### Task 18: Calendar + Event Detail + Apply 페이지

**Files:**
- Create: `frontend/src/pages/CalendarPage.tsx`
- Create: `frontend/src/pages/EventDetailPage.tsx`
- Create: `frontend/src/components/calendar/Calendar.tsx`
- Create: `frontend/src/components/event/EventCard.tsx`
- Create: `frontend/src/components/event/CapacityBar.tsx`
- Create: `frontend/src/api/event.ts`
- Create: `frontend/src/api/application.ts`
- Create: `frontend/src/api/payment.ts`
- Create: `frontend/src/hooks/useEvents.ts`

- [ ] **Step 1: Calendar 컴포넌트** — 월별 달력, 이벤트 있는 날짜 dot 표시
- [ ] **Step 2: CalendarPage** — Calendar + 이벤트 카드 리스트
- [ ] **Step 3: EventCard** — 날짜 박스 + 제목 + 잔여석 태그
- [ ] **Step 4: CapacityBar** — 남/여 잔여석 프로그레스 바
- [ ] **Step 5: EventDetailPage** — 상세 정보 + 신청하기 → 토스페이먼츠 결제
- [ ] **Step 6: API + hooks 작성**
- [ ] **Step 7: Commit**

```bash
git commit -m "feat: add calendar, event detail, application pages"
```

### Task 19: Choice + Match Result 페이지

**Files:**
- Create: `frontend/src/pages/ChoicePage.tsx`
- Create: `frontend/src/pages/MatchResultPage.tsx`
- Create: `frontend/src/api/matching.ts`
- Create: `frontend/src/hooks/useMatching.ts`

- [ ] **Step 1: ChoicePage** — 이성 번호 목록 + 최대 3명 선택 UI
- [ ] **Step 2: MatchResultPage** — 매칭 결과 표시 (매칭됨/안됨)
- [ ] **Step 3: API + hooks 작성**
- [ ] **Step 4: Commit**

```bash
git commit -m "feat: add choice and match result pages"
```

### Task 20: Bar 페이지 (실시간 현황 + 예약)

**Files:**
- Create: `frontend/src/pages/BarStatusPage.tsx`
- Create: `frontend/src/pages/BarReservePage.tsx`
- Create: `frontend/src/components/bar/BarStatusCard.tsx`
- Create: `frontend/src/api/bar.ts`
- Create: `frontend/src/hooks/useBar.ts`

- [ ] **Step 1: BarStatusCard** — 남/여 인원 + 잔여석 + 영업 상태
- [ ] **Step 2: BarStatusPage** — 실시간 현황 (주기적 polling) + 예약 버튼
- [ ] **Step 3: BarReservePage** — 날짜/시간 선택 + 예약 폼
- [ ] **Step 4: API + hooks 작성**
- [ ] **Step 5: Commit**

```bash
git commit -m "feat: add bar status and reservation pages"
```

### Task 21: MyPage + Notifications + Admin 페이지

**Files:**
- Create: `frontend/src/pages/MyPage.tsx`
- Create: `frontend/src/pages/MyApplicationsPage.tsx`
- Create: `frontend/src/pages/ProfileEditPage.tsx`
- Create: `frontend/src/pages/NotificationsPage.tsx`
- Create: `frontend/src/pages/admin/AdminEventsPage.tsx`
- Create: `frontend/src/pages/admin/AdminApplicationsPage.tsx`
- Create: `frontend/src/pages/admin/AdminMembersPage.tsx`
- Create: `frontend/src/pages/admin/AdminBarPage.tsx`
- Create: `frontend/src/api/notification.ts`
- Create: `frontend/src/hooks/useNotifications.ts`
- Create: `frontend/src/hooks/useApplications.ts`

- [ ] **Step 1: MyPage** — 프로필 요약 + 메뉴 (프로필 수정, 신청 내역)
- [ ] **Step 2: MyApplicationsPage** — 내 신청 목록 + 상태 표시
- [ ] **Step 3: ProfileEditPage** — 프로필 수정 폼
- [ ] **Step 4: NotificationsPage** — 알림 목록 (읽음/안읽음 구분)
- [ ] **Step 5: AdminEventsPage** — 이벤트 CRUD
- [ ] **Step 6: AdminApplicationsPage** — 신청 목록 + 프로필 확인 + 승인/거절
- [ ] **Step 7: AdminMembersPage** — 전체 회원 목록
- [ ] **Step 8: AdminBarPage** — 혼술바 영업 시작/종료 + 입퇴장 처리
- [ ] **Step 9: Commit**

```bash
git commit -m "feat: add mypage, notifications, admin pages"
```

### Task 22: App.tsx 라우팅 완성 + 최종 통합

**Files:**
- Modify: `frontend/src/App.tsx`

- [ ] **Step 1: 모든 페이지 라우팅 연결**

```tsx
// frontend/src/App.tsx
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import AppLayout from './components/layout/AppLayout'
import ProtectedRoute from './components/auth/ProtectedRoute'
import LoginPage from './pages/LoginPage'
import KakaoCallbackPage from './pages/KakaoCallbackPage'
import ProfileSetupPage from './pages/ProfileSetupPage'
import CalendarPage from './pages/CalendarPage'
import EventDetailPage from './pages/EventDetailPage'
import ChoicePage from './pages/ChoicePage'
import MatchResultPage from './pages/MatchResultPage'
import MyPage from './pages/MyPage'
import MyApplicationsPage from './pages/MyApplicationsPage'
import ProfileEditPage from './pages/ProfileEditPage'
import BarStatusPage from './pages/BarStatusPage'
import BarReservePage from './pages/BarReservePage'
import NotificationsPage from './pages/NotificationsPage'
import AdminEventsPage from './pages/admin/AdminEventsPage'
import AdminApplicationsPage from './pages/admin/AdminApplicationsPage'
import AdminMembersPage from './pages/admin/AdminMembersPage'
import AdminBarPage from './pages/admin/AdminBarPage'

const queryClient = new QueryClient()

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/auth/kakao/callback" element={<KakaoCallbackPage />} />
          <Route element={<ProtectedRoute />}>
            <Route path="/profile/setup" element={<ProfileSetupPage />} />
            <Route element={<AppLayout />}>
              <Route path="/" element={<CalendarPage />} />
              <Route path="/events/:id" element={<EventDetailPage />} />
              <Route path="/events/:id/choose" element={<ChoicePage />} />
              <Route path="/events/:id/result" element={<MatchResultPage />} />
              <Route path="/mypage" element={<MyPage />} />
              <Route path="/mypage/applications" element={<MyApplicationsPage />} />
              <Route path="/mypage/profile" element={<ProfileEditPage />} />
              <Route path="/bar" element={<BarStatusPage />} />
              <Route path="/bar/reserve" element={<BarReservePage />} />
              <Route path="/notifications" element={<NotificationsPage />} />
              <Route path="/admin/events" element={<AdminEventsPage />} />
              <Route path="/admin/applications" element={<AdminApplicationsPage />} />
              <Route path="/admin/members" element={<AdminMembersPage />} />
              <Route path="/admin/bar" element={<AdminBarPage />} />
            </Route>
          </Route>
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  )
}

export default App
```

- [ ] **Step 2: 빌드 확인**

```bash
cd frontend && npm run build
```

Expected: 빌드 성공

- [ ] **Step 3: Commit**

```bash
git commit -m "feat: complete frontend routing and integration"
```

---

## Chunk 8: Docker + 최종 통합

### Task 23: Docker Compose 환경 구성

**Files:**
- Create: `docker-compose.yml`
- Create: `backend/Dockerfile`
- Create: `frontend/Dockerfile`
- Create: `.gitignore`

- [ ] **Step 1: docker-compose.yml** — MySQL, Redis, backend, frontend 컨테이너
- [ ] **Step 2: backend Dockerfile** — Gradle 빌드 + JDK 17 런타임
- [ ] **Step 3: frontend Dockerfile** — npm build + nginx 서빙
- [ ] **Step 4: .gitignore** — node_modules, build, .gradle, .superpowers 등
- [ ] **Step 5: docker-compose up 으로 전체 실행 확인**
- [ ] **Step 6: Commit**

```bash
git commit -m "feat: add Docker Compose setup for local development"
```
