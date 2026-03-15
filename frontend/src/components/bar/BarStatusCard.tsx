import type { BarStatus } from '../../types'

interface BarStatusCardProps {
  status: BarStatus
}

const BarStatusCard: React.FC<BarStatusCardProps> = ({ status }) => {
  const usedSeats = status.currentMaleCount + status.currentFemaleCount
  const occupancyPct = status.totalSeats > 0
    ? Math.round((usedSeats / status.totalSeats) * 100)
    : 0

  return (
    <div
      style={{
        background: status.isOpen ? 'var(--gradient)' : '#9e9e9e',
        borderRadius: 'var(--radius)',
        padding: '24px 20px',
        color: '#fff',
        marginBottom: 16,
        boxShadow: '0 4px 20px rgba(255,107,138,0.3)',
      }}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 16 }}>
        <div>
          <h2 style={{ fontSize: 20, fontWeight: 800, letterSpacing: '-0.4px' }}>{status.name}</h2>
          <p style={{ fontSize: 12, opacity: 0.85, marginTop: 2 }}>{status.address}</p>
        </div>
        <span
          style={{
            background: status.isOpen ? 'rgba(255,255,255,0.25)' : 'rgba(0,0,0,0.2)',
            padding: '4px 12px',
            borderRadius: 20,
            fontSize: 12,
            fontWeight: 700,
          }}
        >
          {status.isOpen ? '🟢 영업중' : '🔴 마감'}
        </span>
      </div>

      {/* Male / Female counts */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12, marginBottom: 16 }}>
        <div
          style={{
            background: 'rgba(255,255,255,0.2)',
            borderRadius: 10,
            padding: '14px',
            textAlign: 'center',
          }}
        >
          <div style={{ fontSize: 28 }}>👨</div>
          <div style={{ fontSize: 28, fontWeight: 800 }}>{status.currentMaleCount}</div>
          <div style={{ fontSize: 11, opacity: 0.85 }}>남성</div>
        </div>
        <div
          style={{
            background: 'rgba(255,255,255,0.2)',
            borderRadius: 10,
            padding: '14px',
            textAlign: 'center',
          }}
        >
          <div style={{ fontSize: 28 }}>👩</div>
          <div style={{ fontSize: 28, fontWeight: 800 }}>{status.currentFemaleCount}</div>
          <div style={{ fontSize: 11, opacity: 0.85 }}>여성</div>
        </div>
      </div>

      {/* Occupancy bar */}
      <div style={{ marginBottom: 12 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 6 }}>
          <span style={{ fontSize: 12, opacity: 0.85 }}>좌석 현황</span>
          <span style={{ fontSize: 12, fontWeight: 700 }}>
            {usedSeats} / {status.totalSeats}석 ({occupancyPct}%)
          </span>
        </div>
        <div
          style={{
            height: 8,
            background: 'rgba(255,255,255,0.25)',
            borderRadius: 4,
            overflow: 'hidden',
          }}
        >
          <div
            style={{
              height: '100%',
              width: `${occupancyPct}%`,
              background: '#fff',
              borderRadius: 4,
              transition: 'width 0.4s',
            }}
          />
        </div>
      </div>

      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13 }}>
        <span style={{ opacity: 0.85 }}>잔여 좌석</span>
        <span style={{ fontWeight: 800, fontSize: 16 }}>
          {status.remainingSeats}석
        </span>
      </div>

      <div style={{ marginTop: 10, fontSize: 12, opacity: 0.75, textAlign: 'right' }}>
        영업시간: {status.openTime} ~ {status.closeTime}
      </div>
    </div>
  )
}

export default BarStatusCard
