import { Outlet, NavLink, useLocation } from 'react-router-dom'
import { Home, ClipboardList, User } from 'lucide-react'

export default function ParticipantLayout() {
  const location = useLocation()

  const isCafePage = location.pathname.startsWith('/cafes/')
  const isApplicationsPage = location.pathname === '/me/applications'
  const isMyPage =
    location.pathname === '/me' ||
    location.pathname === '/me/profile' ||
    location.pathname === '/me/notifications'

  return (
    <div className="max-w-md mx-auto min-h-screen bg-[#FFF5F5] pb-20">
      <Outlet />

      {/* Bottom Navigation */}
      <nav className="fixed bottom-0 left-0 right-0 z-50">
        <div className="max-w-md mx-auto bg-white border-t border-gray-100 shadow-lg">
          <div className="flex items-center justify-around h-16">
            <NavLink
              to="/me"
              className={() =>
                `flex flex-col items-center justify-center gap-1 w-16 h-12 rounded-xl transition-colors ${
                  isCafePage ? 'text-[#FF6B6B]' : 'text-gray-400'
                }`
              }
            >
              <Home className="w-6 h-6" />
              <span className="text-xs font-medium">홈</span>
            </NavLink>

            <NavLink
              to="/me/applications"
              className={() =>
                `flex flex-col items-center justify-center gap-1 w-16 h-12 rounded-xl transition-colors ${
                  isApplicationsPage ? 'text-[#FF6B6B]' : 'text-gray-400'
                }`
              }
            >
              <ClipboardList className="w-6 h-6" />
              <span className="text-xs font-medium">신청내역</span>
            </NavLink>

            <NavLink
              to="/me"
              className={() =>
                `flex flex-col items-center justify-center gap-1 w-16 h-12 rounded-xl transition-colors ${
                  isMyPage ? 'text-[#FF6B6B]' : 'text-gray-400'
                }`
              }
            >
              <User className="w-6 h-6" />
              <span className="text-xs font-medium">마이</span>
            </NavLink>
          </div>
        </div>
      </nav>
    </div>
  )
}
