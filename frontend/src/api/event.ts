import client from './client'
import type { BlindDateEvent } from '../types'

export const getEventsByMonth = (year: number, month: number) =>
  client.get('/events', { params: { year, month } }).then((r) => r.data as BlindDateEvent[])

export const getEvent = (id: number) =>
  client.get(`/events/${id}`).then((r) => r.data as BlindDateEvent)

export const createEvent = (data: Partial<BlindDateEvent>) =>
  client.post('/events', data).then((r) => r.data)

export const updateEvent = (id: number, data: Partial<BlindDateEvent>) =>
  client.put(`/events/${id}`, data).then((r) => r.data)

export const deleteEvent = (id: number) =>
  client.delete(`/events/${id}`).then((r) => r.data)
