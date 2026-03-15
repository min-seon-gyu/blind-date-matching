import { useLocation, useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { useAuthStore } from '../../stores/authStore'
import { getUnreadCount } from '../../api/notification'

interface NavItem {
  icon: string
  label: string
  path: string
  adminOnly?: boolean
}

const navItems: NavItem[] = [
  { icon: '📅', label: '캘린더', path: '/' },
  { icon: '🍷', label: '혼술바', path: '/bar' },
  { icon: '📋', label: '내 신청', path: '/mypage/applications' },
  { icon: '🔔', label: '알림', path: '/notifications' },
  { icon: '👤', label: '마이', path: '/mypage' },
  { icon: '⚙️', label: '관리', path: '/admin/events', adminOnly: true },
]

const BottomNav = () => {
  const location = useLocation()
  const navigate = useNavigate()
  const member = useAuthStore((s) => s.member)

  const { data: unreadCount = 0 } = useQuery({
    queryKey: ['unreadCount'],
    queryFn: getUnreadCount,
    refetchInterval: 30000,
  })

  const visibleItems = navItems.filter(
    (item) => !item.adminOnly || member?.role === 'ADMIN'
  )

  return (
    <nav
      style={{
        position: 'fixed',
        bottom: 0,
        left: '50%',
        transform: 'translateX(-50%)',
        width: '100%',
        maxWidth: 430,
        background: '#fff',
        borderTop: '1px solid var(--border)',
        display: 'flex',
        zIndex: 100,
        boxShadow: '0 -2px 12px rgba(0,0,0,0.06)',
      }}
    >
      {visibleItems.map((item) => {
        const isActive =
          item.path === '/'
            ? location.pathname === '/'
            : location.pathname.startsWith(item.path)

        return (
          <button
            key={item.path}
            onClick={() => navigate(item.path)}
            style={{
              flex: 1,
              background: 'none',
              border: 'none',
              cursor: 'pointer',
              padding: '8px 4px 10px',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: 2,
              position: 'relative',
            }}
          >
            <span style={{ fontSize: '20px', lineHeight: 1, position: 'relative' }}>
              {item.icon}
              {item.label === '알림' && unreadCount > 0 && (
                <span
                  style={{
                    position: 'absolute',
                    top: -4,
                    right: -6,
                    background: '#ff6b8a',
                    color: '#fff',
                    borderRadius: '50%',
                    width: 14,
                    height: 14,
                    fontSize: '9px',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontWeight: 700,
                  }}
                >
                  {unreadCount > 9 ? '9+' : unreadCount}
                </span>
              )}
            </span>
            <span
              style={{
                fontSize: '10px',
                fontWeight: isActive ? 700 : 400,
                background: isActive ? 'var(--gradient)' : 'none',
                WebkitBackgroundClip: isActive ? 'text' : 'unset',
                WebkitTextFillColor: isActive ? 'transparent' : 'var(--text-light)',
                letterSpacing: '-0.2px',
              }}
            >
              {item.label}
            </span>
            {isActive && (
              <span
                style={{
                  position: 'absolute',
                  top: 0,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: 24,
                  height: 2,
                  background: 'var(--gradient)',
                  borderRadius: 2,
                }}
              />
            )}
          </button>
        )
      })}
    </nav>
  )
}

export default BottomNav
