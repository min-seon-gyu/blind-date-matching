import client from './client'
import type { Notification } from '../types'

export const getNotifications = () =>
  client.get('/notifications').then((r) => r.data as Notification[])

export const markAsRead = (id: number) =>
  client.patch(`/notifications/${id}/read`).then((r) => r.data)

export const getUnreadCount = () =>
  client.get('/notifications/unread-count').then((r) => r.data as number)
