import client from './client'
import type { BarStatus, BarReservation } from '../types'

export const getBarStatus = () =>
  client.get('/bar/status').then((r) => r.data as BarStatus)

export const reserve = (data: { date: string; time: string }) =>
  client.post('/bar/reservations', data).then((r) => r.data)

export const cancelReservation = (id: number) =>
  client.patch(`/bar/reservations/${id}/cancel`).then((r) => r.data)

export const getMyReservations = () =>
  client.get('/bar/reservations/my').then((r) => r.data as BarReservation[])

// Admin
export const openBar = () =>
  client.post('/admin/bar/open').then((r) => r.data)

export const closeBar = () =>
  client.post('/admin/bar/close').then((r) => r.data)

export const checkIn = (data: { gender: string; memberId?: number }) =>
  client.post('/admin/bar/checkin', data).then((r) => r.data)

export const checkOut = (visitorId: number) =>
  client.post(`/admin/bar/checkout/${visitorId}`).then((r) => r.data)

export const getVisitors = () =>
  client.get('/admin/bar/visitors').then((r) => r.data)
