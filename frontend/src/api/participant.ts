import client from './client'
import type { AuthUser, ParticipantProfile, Application, Notification } from '@/types'

export async function getMe(): Promise<AuthUser> {
  const res = await client.get<AuthUser>('/me')
  return res.data
}

export async function getProfile(): Promise<ParticipantProfile> {
  const res = await client.get<ParticipantProfile>('/me/profile')
  return res.data
}

export type ProfilePayload = Omit<ParticipantProfile, 'id'>

export async function createProfile(data: ProfilePayload): Promise<ParticipantProfile> {
  const res = await client.post<ParticipantProfile>('/me/profile', data)
  return res.data
}

export async function updateProfile(data: Partial<ProfilePayload>): Promise<ParticipantProfile> {
  const res = await client.put<ParticipantProfile>('/me/profile', data)
  return res.data
}

export async function getMyApplications(): Promise<Application[]> {
  const res = await client.get<Application[]>('/me/applications')
  return res.data
}

export async function getNotifications(): Promise<Notification[]> {
  const res = await client.get<Notification[]>('/me/notifications')
  return res.data
}

export async function markNotificationAsRead(id: number): Promise<void> {
  await client.put(`/notifications/${id}/read`)
}
