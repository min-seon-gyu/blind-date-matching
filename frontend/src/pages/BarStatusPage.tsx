import { useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { getBarStatus, getMyReservations } from '../api/bar'
import BarStatusCard from '../components/bar/BarStatusCard'
import Button from '../components/common/Button'
import type { BarReservation } from '../types'

const reservationStatusLabel: Record<string, { label: string; color: string }> = {
  CONFIRMED: { label: '예약 확정', color: '#2e7d32' },
  CANCELLED: { label: '취소됨', color: '#9e9e9e' },
  VISITED: { label: '방문 완료', color: '#1565c0' },
  NO_SHOW: { label: '노쇼', color: '#b71c1c' },
}

const BarStatusPage = () => {
  const navigate = useNavigate()

  const { data: barStatus, isLoading: statusLoading } = useQuery({
    queryKey: ['barStatus'],
    queryFn: getBarStatus,
    refetchInterval: 10000,
  })

  const { data: reservations = [] } = useQuery({
    queryKey: ['myReservations'],
    queryFn: getMyReservations,
  })

  return (
    <div style={{ padding: '20px 16px' }}>
      <div style={{ marginBottom: 20 }}>
        <h1
          style={{
            fontSize: 22,
            fontWeight: 800,
            letterSpacing: '-0.5px',
            background: 'var(--gradient)',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
            marginBottom: 4,
          }}
        >
          🍷 혼술바
        </h1>
        <p style={{ fontSize: 13, color: 'var(--text-light)' }}>
          실시간 현황 (10초마다 자동 갱신)
        </p>
      </div>

      {statusLoading ? (
        <div
          style={{
            background: '#eee',
            borderRadius: 'var(--radius)',
            height: 200,
            marginBottom: 16,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: 'var(--text-light)',
          }}
        >
          불러오는 중...
        </div>
      ) : barStatus ? (
        <BarStatusCard status={barStatus} />
      ) : (
        <div
          style={{
            background: '#fff',
            borderRadius: 'var(--radius)',
            padding: 24,
            textAlign: 'center',
            marginBottom: 16,
          }}
        >
          <p style={{ color: 'var(--text-light)' }}>바 정보를 불러올 수 없습니다.</p>
        </div>
      )}

      {barStatus?.isOpen && (
        <Button fullWidth onClick={() => navigate('/bar/reserve')}>
          예약하기
        </Button>
      )}

      {/* My reservations */}
      {reservations.length > 0 && (
        <div style={{ marginTop: 24 }}>
          <h2 style={{ fontSize: 16, fontWeight: 700, marginBottom: 12, color: 'var(--text)' }}>
            내 예약 내역
          </h2>
          {reservations.map((r: BarReservation) => {
            const st = reservationStatusLabel[r.status] ?? { label: r.status, color: '#666' }
            return (
              <div
                key={r.id}
                style={{
                  background: '#fff',
                  borderRadius: 12,
                  padding: '14px 16px',
                  boxShadow: 'var(--shadow)',
                  marginBottom: 8,
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                }}
              >
                <div>
                  <p style={{ fontWeight: 600, fontSize: 14, marginBottom: 2 }}>
                    📅 {r.date}
                  </p>
                  <p style={{ fontSize: 13, color: 'var(--text-light)' }}>⏰ {r.time}</p>
                </div>
                <span
                  style={{
                    fontSize: 12,
                    fontWeight: 700,
                    color: st.color,
                    background: `${st.color}15`,
                    padding: '4px 10px',
                    borderRadius: 20,
                  }}
                >
                  {st.label}
                </span>
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}

export default BarStatusPage
