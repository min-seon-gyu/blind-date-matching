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
