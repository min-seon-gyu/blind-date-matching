import client from './client'
import type { AuthUser } from '@/types'

interface AuthResponse {
  accessToken: string
  user: AuthUser
}

export async function kakaoLogin(code: string): Promise<AuthResponse> {
  const res = await client.post<AuthResponse>('/auth/kakao', { code })
  return res.data
}

export async function refresh(): Promise<AuthResponse> {
  const res = await client.post<AuthResponse>('/auth/refresh')
  return res.data
}
