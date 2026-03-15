import client from './client'
import type { ParticipantInfo, MatchResultResponse } from '../types'

export const getParticipants = (eventId: number) =>
  client.get(`/events/${eventId}/participants`).then((r) => r.data as ParticipantInfo[])

export const submitChoices = (eventId: number, memberIds: number[]) =>
  client.post(`/events/${eventId}/choices`, { memberIds }).then((r) => r.data)

export const getMatchResult = (eventId: number) =>
  client.get(`/events/${eventId}/result`).then((r) => r.data as MatchResultResponse)
