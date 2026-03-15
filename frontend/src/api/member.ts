import client from './client'
import type { MemberProfile } from '../types'

export const getMe = () =>
  client.get('/members/me').then((r) => r.data)

export const getProfile = () =>
  client.get('/members/me/profile').then((r) => r.data as MemberProfile)

export const createProfile = (data: Partial<MemberProfile>) =>
  client.post('/members/me/profile', data).then((r) => r.data)

export const updateProfile = (data: Partial<MemberProfile>) =>
  client.put('/members/me/profile', data).then((r) => r.data)

export const getMembers = () =>
  client.get('/admin/members').then((r) => r.data)
