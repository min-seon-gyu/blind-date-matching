import { useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { getProfile, getNotifications } from '@/api/participant'
import { useAuthStore } from '@/stores/authStore'
import {
  User, Edit, ClipboardList, Bell, LogOut, ChevronRight, Loader2,
} from 'lucide-react'

export default function MyPage() {
  const navigate = useNavigate()
  const logout = useAuthStore((s) => s.logout)

  const { data: profile, isLoading } = useQuery({
    queryKey: ['profile'],
    queryFn: getProfile,
  })

  const { data: notifications } = useQuery({
    queryKey: ['notifications'],
    queryFn: getNotifications,
  })

  const unreadCount = notifications?.filter((n) => !n.isRead).length ?? 0

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <Loader2 className="w-8 h-8 text-[#FF6B6B] animate-spin" />
      </div>
    )
  }

  const menuItems = [
    {
      label: '프로필 수정',
      icon: Edit,
      onClick: () => navigate('/me/profile'),
    },
    {
      label: '신청 내역',
      icon: ClipboardList,
      onClick: () => navigate('/me/applications'),
    },
    {
      label: '알림',
      icon: Bell,
      onClick: () => navigate('/me/notifications'),
      badge: unreadCount > 0 ? unreadCount : undefined,
    },
  ]

  return (
    <div className="px-4 py-6 space-y-5">
      {/* Profile Card */}
      <div className="bg-white rounded-2xl p-5 shadow-sm">
        <div className="flex items-center gap-4">
          <div className="w-16 h-16 rounded-full bg-gradient-to-br from-[#FF6B6B] to-[#FFE66D] flex items-center justify-center overflow-hidden shrink-0">
            {profile?.photoUrl ? (
              <img
                src={profile.photoUrl}
                alt="프로필"
                className="w-full h-full object-cover"
              />
            ) : (
              <User className="w-7 h-7 text-white" />
            )}
          </div>
          <div className="min-w-0">
            <h2 className="text-lg font-bold text-gray-900 truncate">
              {profile?.name ?? '사용자'}
            </h2>
            <p className="text-sm text-gray-500 truncate">
              {profile
                ? `${profile.age}세 / ${profile.job} / ${profile.mbti}`
                : '프로필을 설정해주세요'}
            </p>
            {profile?.introduction && (
              <p className="text-xs text-gray-400 mt-1 line-clamp-1">
                {profile.introduction}
              </p>
            )}
          </div>
        </div>
      </div>

      {/* Menu List */}
      <div className="bg-white rounded-2xl shadow-sm overflow-hidden">
        {menuItems.map((item, i) => (
          <button
            key={item.label}
            onClick={item.onClick}
            className={`w-full flex items-center justify-between px-5 py-4 text-left hover:bg-gray-50 transition-colors ${
              i > 0 ? 'border-t border-gray-50' : ''
            }`}
          >
            <div className="flex items-center gap-3">
              <item.icon className="w-5 h-5 text-gray-500" />
              <span className="text-sm font-medium text-gray-800">
                {item.label}
              </span>
            </div>
            <div className="flex items-center gap-2">
              {item.badge && (
                <span className="w-5 h-5 rounded-full bg-[#FF6B6B] text-white text-xs font-bold flex items-center justify-center">
                  {item.badge > 99 ? '99+' : item.badge}
                </span>
              )}
              <ChevronRight className="w-4 h-4 text-gray-300" />
            </div>
          </button>
        ))}
      </div>

      {/* Logout */}
      <button
        onClick={handleLogout}
        className="w-full flex items-center gap-3 px-5 py-4 bg-white rounded-2xl shadow-sm text-red-500 hover:bg-red-50 transition-colors"
      >
        <LogOut className="w-5 h-5" />
        <span className="text-sm font-medium">로그아웃</span>
      </button>
    </div>
  )
}
