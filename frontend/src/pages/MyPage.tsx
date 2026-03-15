import { useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { useAuthStore } from '../stores/authStore'
import { getProfile } from '../api/member'
import { logout as apiLogout } from '../api/auth'

const MyPage = () => {
  const navigate = useNavigate()
  const { member, logout } = useAuthStore()

  const { data: profile } = useQuery({
    queryKey: ['myProfile'],
    queryFn: getProfile,
    enabled: !!member,
  })

  const handleLogout = async () => {
    try { await apiLogout() } catch { /* ignore */ }
    logout()
    navigate('/login')
  }

  const menuItems = [
    { icon: '✏️', label: '프로필 수정', path: '/mypage/profile' },
    { icon: '📋', label: '내 신청 내역', path: '/mypage/applications' },
    { icon: '🍷', label: '혼술바 예약', path: '/bar' },
  ]

  return (
    <div>
      {/* Profile header */}
      <div
        style={{
          background: 'var(--gradient)',
          padding: '32px 20px 28px',
          color: '#fff',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
          <div
            style={{
              width: 72,
              height: 72,
              background: 'rgba(255,255,255,0.25)',
              borderRadius: '50%',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: 32,
              flexShrink: 0,
              border: '3px solid rgba(255,255,255,0.4)',
            }}
          >
            {profile?.gender === 'MALE' ? '👨' : '👩'}
          </div>
          <div>
            <h1 style={{ fontSize: 22, fontWeight: 800, letterSpacing: '-0.4px', marginBottom: 4 }}>
              {profile?.name ?? '프로필 없음'}
            </h1>
            {profile && (
              <p style={{ fontSize: 14, opacity: 0.9 }}>
                {profile.age}세 &middot; {profile.job} &middot; {profile.mbti}
              </p>
            )}
            {member?.role === 'ADMIN' && (
              <span
                style={{
                  display: 'inline-block',
                  marginTop: 4,
                  background: 'rgba(255,255,255,0.25)',
                  fontSize: 11,
                  padding: '2px 8px',
                  borderRadius: 10,
                  fontWeight: 700,
                }}
              >
                ⚙️ 관리자
              </span>
            )}
          </div>
        </div>

        {/* Profile stats */}
        {profile && (
          <div
            style={{
              display: 'flex',
              gap: 12,
              marginTop: 20,
              background: 'rgba(255,255,255,0.15)',
              borderRadius: 12,
              padding: '12px',
            }}
          >
            {[
              { label: 'MBTI', value: profile.mbti },
              { label: '키', value: `${profile.height}cm` },
              { label: '취미', value: profile.hobby },
            ].map(({ label, value }) => (
              <div key={label} style={{ flex: 1, textAlign: 'center' }}>
                <p style={{ fontSize: 12, opacity: 0.8, marginBottom: 2 }}>{label}</p>
                <p style={{ fontSize: 14, fontWeight: 700 }}>{value}</p>
              </div>
            ))}
          </div>
        )}
      </div>

      <div style={{ padding: '20px 16px' }}>
        {/* Menu items */}
        <div
          style={{
            background: '#fff',
            borderRadius: 'var(--radius)',
            boxShadow: 'var(--shadow)',
            overflow: 'hidden',
            marginBottom: 16,
          }}
        >
          {menuItems.map((item, idx) => (
            <button
              key={item.path}
              onClick={() => navigate(item.path)}
              style={{
                width: '100%',
                background: 'none',
                border: 'none',
                borderBottom: idx < menuItems.length - 1 ? '1px solid var(--border)' : 'none',
                padding: '16px 18px',
                display: 'flex',
                alignItems: 'center',
                gap: 12,
                cursor: 'pointer',
                fontFamily: 'inherit',
              }}
            >
              <span style={{ fontSize: 20, width: 28 }}>{item.icon}</span>
              <span style={{ fontSize: 15, fontWeight: 500, color: 'var(--text)', flex: 1, textAlign: 'left' }}>
                {item.label}
              </span>
              <span style={{ color: 'var(--text-light)', fontSize: 16 }}>›</span>
            </button>
          ))}
        </div>

        {/* Admin shortcut */}
        {member?.role === 'ADMIN' && (
          <div
            style={{
              background: 'var(--admin-gradient)',
              borderRadius: 'var(--radius)',
              padding: 16,
              marginBottom: 16,
            }}
          >
            <p style={{ color: '#fff', fontSize: 13, fontWeight: 600, marginBottom: 10 }}>관리자 메뉴</p>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8 }}>
              {[
                { label: '이벤트', path: '/admin/events' },
                { label: '신청 관리', path: '/admin/applications' },
                { label: '회원 관리', path: '/admin/members' },
                { label: '바 관리', path: '/admin/bar' },
              ].map((item) => (
                <button
                  key={item.path}
                  onClick={() => navigate(item.path)}
                  style={{
                    background: 'rgba(255,255,255,0.2)',
                    border: 'none',
                    borderRadius: 8,
                    padding: '10px',
                    color: '#fff',
                    fontSize: 13,
                    fontWeight: 600,
                    cursor: 'pointer',
                    fontFamily: 'inherit',
                  }}
                >
                  {item.label}
                </button>
              ))}
            </div>
          </div>
        )}

        {/* Logout */}
        <button
          onClick={handleLogout}
          style={{
            width: '100%',
            background: '#fff',
            border: '1.5px solid #ffcdd2',
            borderRadius: 12,
            padding: '14px',
            color: '#e53935',
            fontSize: 15,
            fontWeight: 600,
            cursor: 'pointer',
            fontFamily: 'inherit',
          }}
        >
          🚪 로그아웃
        </button>
      </div>
    </div>
  )
}

export default MyPage
