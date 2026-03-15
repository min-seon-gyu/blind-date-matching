import { useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getNotifications, markAsRead } from '../api/notification'
import type { Notification, NotificationType } from '../types'

const typeIcon: Record<NotificationType, string> = {
  NEW_APPLICATION: '📋',
  APPROVED: '✅',
  REJECTED: '❌',
  MATCH_RESULT: '💕',
  EVENT_REMINDER: '📅',
}

const NotificationsPage = () => {
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const { data: notifications = [], isLoading } = useQuery({
    queryKey: ['notifications'],
    queryFn: getNotifications,
  })

  const markMutation = useMutation({
    mutationFn: markAsRead,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] })
      queryClient.invalidateQueries({ queryKey: ['unreadCount'] })
    },
  })

  return (
    <div style={{ padding: '20px 16px' }}>
      <div style={{ marginBottom: 20 }}>
        <button
          onClick={() => navigate(-1)}
          style={{
            background: 'none',
            border: 'none',
            fontSize: 14,
            color: 'var(--text-light)',
            cursor: 'pointer',
            fontFamily: 'inherit',
            marginBottom: 12,
          }}
        >
          ← 뒤로
        </button>
        <h1
          style={{
            fontSize: 22,
            fontWeight: 800,
            letterSpacing: '-0.5px',
            background: 'var(--gradient)',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
          }}
        >
          🔔 알림
        </h1>
      </div>

      {isLoading ? (
        <div style={{ textAlign: 'center', padding: 40, color: 'var(--text-light)' }}>
          <p>불러오는 중...</p>
        </div>
      ) : notifications.length === 0 ? (
        <div
          style={{
            textAlign: 'center',
            padding: '50px 20px',
            background: '#fff',
            borderRadius: 'var(--radius)',
            boxShadow: 'var(--shadow)',
          }}
        >
          <div style={{ fontSize: 48, marginBottom: 12 }}>🔕</div>
          <p style={{ color: 'var(--text-light)' }}>알림이 없습니다.</p>
        </div>
      ) : (
        notifications.map((n: Notification) => (
          <div
            key={n.id}
            onClick={() => {
              if (!n.isRead) markMutation.mutate(n.id)
            }}
            style={{
              background: n.isRead ? '#fff' : '#fff0f4',
              borderRadius: 12,
              padding: '14px 16px',
              marginBottom: 8,
              boxShadow: 'var(--shadow)',
              border: `1px solid ${n.isRead ? 'var(--border)' : 'var(--primary)'}`,
              cursor: n.isRead ? 'default' : 'pointer',
              display: 'flex',
              gap: 12,
              alignItems: 'flex-start',
              transition: 'background 0.2s',
            }}
          >
            <div
              style={{
                width: 44,
                height: 44,
                background: n.isRead ? '#f5f5f5' : 'linear-gradient(135deg, #fff0f4, #fff4ef)',
                borderRadius: '50%',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: 22,
                flexShrink: 0,
                border: n.isRead ? 'none' : '1.5px solid var(--primary)',
              }}
            >
              {typeIcon[n.type] ?? '🔔'}
            </div>
            <div style={{ flex: 1 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
                <p
                  style={{
                    fontWeight: n.isRead ? 500 : 700,
                    fontSize: 14,
                    color: 'var(--text)',
                    letterSpacing: '-0.2px',
                  }}
                >
                  {n.title}
                </p>
                {!n.isRead && (
                  <span
                    style={{
                      width: 8,
                      height: 8,
                      background: 'var(--primary)',
                      borderRadius: '50%',
                      flexShrink: 0,
                      marginTop: 4,
                    }}
                  />
                )}
              </div>
              <p style={{ fontSize: 13, color: 'var(--text-light)', lineHeight: 1.5, marginBottom: 6 }}>
                {n.message}
              </p>
              <p style={{ fontSize: 11, color: 'var(--text-light)' }}>
                {new Date(n.createdAt).toLocaleString()}
              </p>
            </div>
          </div>
        ))
      )}
    </div>
  )
}

export default NotificationsPage
