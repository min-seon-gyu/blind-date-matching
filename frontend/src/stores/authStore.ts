import { create } from 'zustand'

interface AuthMember {
  id: number
  role: string
  hasProfile: boolean
}

interface AuthState {
  accessToken: string | null
  member: AuthMember | null
  setAuth: (accessToken: string, member: AuthMember) => void
  logout: () => void
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: null,
  member: null,
  setAuth: (accessToken, member) => set({ accessToken, member }),
  logout: () => set({ accessToken: null, member: null }),
}))
