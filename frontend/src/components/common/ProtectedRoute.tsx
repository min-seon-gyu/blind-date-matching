import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuthStore } from '@/stores/authStore'
import type { UserType } from '@/types'

interface ProtectedRouteProps {
  userType: UserType | UserType[]
}

export default function ProtectedRoute({ userType }: ProtectedRouteProps) {
  const { accessToken, user } = useAuthStore()
  const location = useLocation()

  if (!accessToken || !user) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  const allowedTypes = Array.isArray(userType) ? userType : [userType]
  if (!allowedTypes.includes(user.userType)) {
    return <Navigate to="/login" replace />
  }

  if (
    user.userType === 'PARTICIPANT' &&
    user.hasProfile === false &&
    location.pathname !== '/profile/setup'
  ) {
    return <Navigate to="/profile/setup" replace />
  }

  return <Outlet />
}
