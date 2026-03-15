import client from './client'

export const kakaoLogin = (code: string) =>
  client.post('/auth/kakao/login', { code }).then((r) => r.data)

export const refresh = () =>
  client.post('/auth/refresh').then((r) => r.data)

export const logout = () =>
  client.post('/auth/logout').then((r) => r.data)
