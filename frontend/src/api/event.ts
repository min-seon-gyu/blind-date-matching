import client from './client'
import type { Cafe, Event, ParticipantInfo, MatchResultResponse } from '@/types'

export async function getCafe(slug: string): Promise<Cafe> {
  const res = await client.get<Cafe>(`/cafes/${slug}`)
  return res.data
}

export async function getCafeEvents(slug: string): Promise<Event[]> {
  const res = await client.get<Event[]>(`/cafes/${slug}/events`)
  return res.data
}

export async function getEvent(slug: string, eventId: number): Promise<Event> {
  const res = await client.get<Event>(`/cafes/${slug}/events/${eventId}`)
  return res.data
}

export async function applyEvent(eventId: number): Promise<void> {
  await client.post(`/events/${eventId}/apply`)
}

export async function cancelApplication(eventId: number): Promise<void> {
  await client.delete(`/events/${eventId}/apply`)
}

export async function getParticipants(eventId: number): Promise<ParticipantInfo[]> {
  const res = await client.get<ParticipantInfo[]>(`/events/${eventId}/participants`)
  return res.data
}

export async function submitChoices(eventId: number, participantIds: number[]): Promise<void> {
  await client.post(`/events/${eventId}/choices`, { participantIds })
}

export async function updateChoices(eventId: number, participantIds: number[]): Promise<void> {
  await client.put(`/events/${eventId}/choices`, { participantIds })
}

export async function getMatchResult(eventId: number): Promise<MatchResultResponse> {
  const res = await client.get<MatchResultResponse>(`/events/${eventId}/result`)
  return res.data
}
