import { useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { getUnreadCount } from '../../api/notification'

const Header = () => {
  const navigate = useNavigate()

  const { data: unreadCount = 0 } = useQuery({
    queryKey: ['unreadCount'],
    queryFn: getUnreadCount,
    refetchInterval: 30000,
  })

  return (
    <header
      style={{
        position: 'sticky',
        top: 0,
        zIndex: 100,
        background: '#fff',
        borderBottom: '1px solid var(--border)',
        padding: '0 16px',
        height: 56,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        boxShadow: '0 1px 8px rgba(0,0,0,0.05)',
      }}
    >
      <h1
        style={{
          fontSize: '22px',
          fontWeight: 700,
          background: 'var(--gradient)',
          WebkitBackgroundClip: 'text',
          WebkitTextFillColor: 'transparent',
          cursor: 'pointer',
          letterSpacing: '-0.5px',
        }}
        onClick={() => navigate('/')}
      >
        💕 소개팅
      </h1>

      <button
        onClick={() => navigate('/notifications')}
        style={{
          background: 'none',
          border: 'none',
          cursor: 'pointer',
          position: 'relative',
          padding: '8px',
          fontSize: '22px',
          lineHeight: 1,
        }}
        aria-label="알림"
      >
        🔔
        {unreadCount > 0 && (
          <span
            style={{
              position: 'absolute',
              top: 4,
              right: 4,
              background: 'var(--gradient)',
              color: '#fff',
              borderRadius: '50%',
              width: 18,
              height: 18,
              fontSize: '10px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontWeight: 700,
            }}
          >
            {unreadCount > 9 ? '9+' : unreadCount}
          </span>
        )}
      </button>
    </header>
  )
}

export default Header
