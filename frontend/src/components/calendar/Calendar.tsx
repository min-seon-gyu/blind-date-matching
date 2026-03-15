import type { BlindDateEvent } from '../../types'

interface CalendarProps {
  year: number
  month: number
  events: BlindDateEvent[]
  selectedDate: string | null
  onDateSelect: (date: string | null) => void
  onMonthChange: (year: number, month: number) => void
}

const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토']

const Calendar: React.FC<CalendarProps> = ({
  year,
  month,
  events,
  selectedDate,
  onDateSelect,
  onMonthChange,
}) => {
  const eventDates = new Set(events.map((e) => e.date.slice(0, 10)))

  const firstDay = new Date(year, month - 1, 1).getDay()
  const daysInMonth = new Date(year, month, 0).getDate()

  const cells: (number | null)[] = [
    ...Array(firstDay).fill(null),
    ...Array.from({ length: daysInMonth }, (_, i) => i + 1),
  ]

  const pad = (n: number) => String(n).padStart(2, '0')

  const today = new Date()
  const isToday = (d: number) =>
    today.getFullYear() === year &&
    today.getMonth() + 1 === month &&
    today.getDate() === d

  const handlePrev = () => {
    if (month === 1) onMonthChange(year - 1, 12)
    else onMonthChange(year, month - 1)
  }

  const handleNext = () => {
    if (month === 12) onMonthChange(year + 1, 1)
    else onMonthChange(year, month + 1)
  }

  return (
    <div
      style={{
        background: '#fff',
        borderRadius: 'var(--radius)',
        padding: '16px',
        boxShadow: 'var(--shadow)',
        marginBottom: 16,
      }}
    >
      {/* Month nav */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          marginBottom: 14,
        }}
      >
        <button
          onClick={handlePrev}
          style={{
            background: 'none',
            border: 'none',
            fontSize: 20,
            cursor: 'pointer',
            padding: '4px 8px',
            color: 'var(--text-light)',
          }}
        >
          ‹
        </button>
        <h2
          style={{
            fontSize: 18,
            fontWeight: 700,
            background: 'var(--gradient)',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
          }}
        >
          {year}년 {month}월
        </h2>
        <button
          onClick={handleNext}
          style={{
            background: 'none',
            border: 'none',
            fontSize: 20,
            cursor: 'pointer',
            padding: '4px 8px',
            color: 'var(--text-light)',
          }}
        >
          ›
        </button>
      </div>

      {/* Weekday headers */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(7, 1fr)',
          marginBottom: 6,
        }}
      >
        {WEEKDAYS.map((d, i) => (
          <div
            key={d}
            style={{
              textAlign: 'center',
              fontSize: 11,
              fontWeight: 600,
              color: i === 0 ? '#ff6b8a' : i === 6 ? '#4a90d9' : 'var(--text-light)',
              paddingBottom: 4,
            }}
          >
            {d}
          </div>
        ))}
      </div>

      {/* Days grid */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(7, 1fr)',
          gap: '2px',
        }}
      >
        {cells.map((day, idx) => {
          if (!day) return <div key={`empty-${idx}`} />
          const dateStr = `${year}-${pad(month)}-${pad(day)}`
          const hasEvent = eventDates.has(dateStr)
          const isSelected = selectedDate === dateStr
          const todayDay = isToday(day)
          const col = idx % 7

          return (
            <button
              key={dateStr}
              onClick={() => onDateSelect(isSelected ? null : dateStr)}
              style={{
                padding: '6px 2px',
                background: isSelected
                  ? 'var(--gradient)'
                  : todayDay
                  ? '#fff0f4'
                  : 'transparent',
                border: todayDay && !isSelected ? '1.5px solid var(--primary)' : 'none',
                borderRadius: 8,
                cursor: 'pointer',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                gap: 2,
                fontFamily: 'inherit',
              }}
            >
              <span
                style={{
                  fontSize: 13,
                  fontWeight: isSelected || todayDay ? 700 : 400,
                  color: isSelected
                    ? '#fff'
                    : col === 0
                    ? '#ff6b8a'
                    : col === 6
                    ? '#4a90d9'
                    : 'var(--text)',
                }}
              >
                {day}
              </span>
              {hasEvent && (
                <span
                  style={{
                    width: 5,
                    height: 5,
                    borderRadius: '50%',
                    background: isSelected ? '#fff' : 'var(--primary)',
                    display: 'block',
                  }}
                />
              )}
            </button>
          )
        })}
      </div>
    </div>
  )
}

export default Calendar
