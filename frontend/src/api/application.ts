import client from './client'
import type { Application } from '../types'

export const apply = (eventId: number) =>
  client.post(`/events/${eventId}/apply`).then((r) => r.data)

export const cancelApplication = (id: number) =>
  client.patch(`/applications/${id}/cancel`).then((r) => r.data)

export const getMyApplications = () =>
  client.get('/applications/my').then((r) => r.data as Application[])

export const getApplicationsByEvent = (eventId: number) =>
  client.get(`/admin/applications`, { params: { eventId } }).then((r) => r.data)

export const approveApplication = (id: number) =>
  client.patch(`/admin/applications/${id}/approve`).then((r) => r.data)

export const rejectApplication = (id: number, reason: string) =>
  client.patch(`/admin/applications/${id}/reject`, { reason }).then((r) => r.data)
