import { useState } from 'react'
import Calendar from '../components/calendar/Calendar'
import EventCard from '../components/event/EventCard'
import { useEventsByMonth } from '../hooks/useEvents'
import type { BlindDateEvent } from '../types'

const CalendarPage = () => {
  const today = new Date()
  const [year, setYear] = useState(today.getFullYear())
  const [month, setMonth] = useState(today.getMonth() + 1)
  const [selectedDate, setSelectedDate] = useState<string | null>(null)

  const { data: events = [], isLoading } = useEventsByMonth(year, month)

  const filteredEvents: BlindDateEvent[] = selectedDate
    ? events.filter((e) => e.date.slice(0, 10) === selectedDate)
    : events

  return (
    <div style={{ padding: '16px' }}>
      <Calendar
        year={year}
        month={month}
        events={events}
        selectedDate={selectedDate}
        onDateSelect={setSelectedDate}
        onMonthChange={(y, m) => {
          setYear(y)
          setMonth(m)
          setSelectedDate(null)
        }}
      />

      <div>
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            marginBottom: 12,
          }}
        >
          <h2
            style={{
              fontSize: 16,
              fontWeight: 700,
              color: 'var(--text)',
              letterSpacing: '-0.3px',
            }}
          >
            {selectedDate ? `${selectedDate} 이벤트` : '이번 달 소개팅'}
          </h2>
          {selectedDate && (
            <button
              onClick={() => setSelectedDate(null)}
              style={{
                background: 'none',
                border: 'none',
                fontSize: 13,
                color: 'var(--primary)',
                cursor: 'pointer',
                fontFamily: 'inherit',
                fontWeight: 600,
              }}
            >
              전체보기
            </button>
          )}
        </div>

        {isLoading ? (
          <div style={{ textAlign: 'center', padding: '40px', color: 'var(--text-light)' }}>
            <div style={{ fontSize: 32, marginBottom: 8 }}>🔍</div>
            <p>불러오는 중...</p>
          </div>
        ) : filteredEvents.length === 0 ? (
          <div
            style={{
              textAlign: 'center',
              padding: '40px 20px',
              background: '#fff',
              borderRadius: 'var(--radius)',
              boxShadow: 'var(--shadow)',
            }}
          >
            <div style={{ fontSize: 40, marginBottom: 10 }}>📅</div>
            <p style={{ color: 'var(--text-light)', fontSize: 14 }}>
              {selectedDate ? '이 날짜에 이벤트가 없습니다.' : '이번 달 이벤트가 없습니다.'}
            </p>
          </div>
        ) : (
          filteredEvents.map((event) => (
            <EventCard key={event.id} event={event} />
          ))
        )}
      </div>
    </div>
  )
}

export default CalendarPage
