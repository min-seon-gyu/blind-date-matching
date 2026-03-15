import { useNavigate } from 'react-router-dom'
import type { BlindDateEvent } from '../../types'

interface EventCardProps {
  event: BlindDateEvent
}

const statusColors: Record<string, { bg: string; color: string; label: string }> = {
  OPEN: { bg: '#e8f5e9', color: '#2e7d32', label: '모집중' },
  CLOSED: { bg: '#fff3e0', color: '#e65100', label: '마감' },
  COMPLETED: { bg: '#f3e5f5', color: '#6a1b9a', label: '완료' },
}

const EventCard: React.FC<EventCardProps> = ({ event }) => {
  const navigate = useNavigate()
  const dateObj = new Date(event.date)
  const month = dateObj.getMonth() + 1
  const day = dateObj.getDate()
  const weekdays = ['일', '월', '화', '수', '목', '금', '토']
  const weekday = weekdays[dateObj.getDay()]
  const status = statusColors[event.status] ?? statusColors.CLOSED

  const maleRemaining = event.maleCapacity - event.currentMaleCount
  const femaleRemaining = event.femaleCapacity - event.currentFemaleCount

  return (
    <div
      onClick={() => navigate(`/events/${event.id}`)}
      style={{
        background: '#fff',
        borderRadius: 'var(--radius)',
        padding: '16px',
        boxShadow: 'var(--shadow)',
        display: 'flex',
        alignItems: 'center',
        gap: 14,
        cursor: 'pointer',
        marginBottom: 10,
        border: '1px solid var(--border)',
        transition: 'transform 0.15s, box-shadow 0.15s',
      }}
      onMouseEnter={(e) => {
        e.currentTarget.style.transform = 'translateY(-1px)'
        e.currentTarget.style.boxShadow = '0 4px 18px rgba(0,0,0,0.1)'
      }}
      onMouseLeave={(e) => {
        e.currentTarget.style.transform = 'translateY(0)'
        e.currentTarget.style.boxShadow = 'var(--shadow)'
      }}
    >
      {/* Date badge */}
      <div
        style={{
          minWidth: 52,
          height: 56,
          background: 'var(--gradient)',
          borderRadius: 10,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          color: '#fff',
          flexShrink: 0,
        }}
      >
        <span style={{ fontSize: 11, fontWeight: 500, opacity: 0.85 }}>{month}월</span>
        <span style={{ fontSize: 22, fontWeight: 800, lineHeight: 1 }}>{day}</span>
        <span style={{ fontSize: 10, opacity: 0.85 }}>{weekday}요일</span>
      </div>

      {/* Info */}
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: 4 }}>
          <span
            style={{
              background: status.bg,
              color: status.color,
              fontSize: 10,
              fontWeight: 700,
              padding: '2px 7px',
              borderRadius: 6,
            }}
          >
            {status.label}
          </span>
        </div>
        <h3
          style={{
            fontSize: 15,
            fontWeight: 700,
            color: 'var(--text)',
            letterSpacing: '-0.3px',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            whiteSpace: 'nowrap',
            marginBottom: 4,
          }}
        >
          {event.title}
        </h3>
        <p style={{ fontSize: 12, color: 'var(--text-light)' }}>
          ⏰ {event.time} &nbsp;·&nbsp; 💰 {event.price.toLocaleString()}원
        </p>
      </div>

      {/* Capacity */}
      <div style={{ textAlign: 'right', flexShrink: 0 }}>
        <div style={{ fontSize: 11, color: '#4a90d9', fontWeight: 600, marginBottom: 2 }}>
          👨 {maleRemaining > 0 ? `${maleRemaining}자리` : '마감'}
        </div>
        <div style={{ fontSize: 11, color: '#ff6b8a', fontWeight: 600 }}>
          👩 {femaleRemaining > 0 ? `${femaleRemaining}자리` : '마감'}
        </div>
      </div>
    </div>
  )
}

export default EventCard
