export type UserType = 'PARTICIPANT' | 'ORGANIZER' | 'CAFE_OWNER' | 'PLATFORM_ADMIN'
export type Gender = 'MALE' | 'FEMALE'
export type DrinkingType = 'NONE' | 'SOMETIMES' | 'OFTEN'
export type SmokingType = 'NONE' | 'SOMETIMES' | 'OFTEN'
export type EventStatus = 'OPEN' | 'CLOSED' | 'COMPLETED'
export type ApplicationStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED' | 'COMPLETED'
export type NotificationType =
  | 'NEW_APPLICATION'
  | 'APPROVED'
  | 'REJECTED'
  | 'MATCH_RESULT'
  | 'EVENT_REMINDER'
  | 'CHOICE_REMINDER'
  | 'EVENT_COMPLETED'
  | 'COMMISSION_INVOICE'
  | 'PARTNERSHIP_REQUESTED'
  | 'PARTNERSHIP_ACCEPTED'
  | 'PARTNERSHIP_REJECTED'

export interface Cafe {
  id: number
  name: string
  address: string
  description: string | null
  logoUrl: string | null
  coverImageUrl: string | null
  slug: string
  latitude: number | null
  longitude: number | null
  isActive: boolean
}

export interface Event {
  id: number
  cafeId: number
  organizerId: number
  title: string
  date: string
  time: string
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

export interface ParticipantProfile {
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

export interface Application {
  id: number
  participantId: number
  eventId: number
  status: ApplicationStatus
  appliedAt: string
  reviewedAt: string | null
  rejectReason: string | null
  eventTitle?: string
  cafeName?: string
  eventDate?: string
}

export interface MatchResultResponse {
  matched: boolean
  partnerNickname: string | null
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

export interface ParticipantInfo {
  number: number
  gender: Gender
  age: number
  job: string
  introduction: string | null
}

export interface AuthUser {
  id: number
  userType: UserType
  hasProfile?: boolean
  cafeId?: number
}
