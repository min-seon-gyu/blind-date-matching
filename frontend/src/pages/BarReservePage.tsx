import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { reserve } from '../api/bar'
import Button from '../components/common/Button'

const BarReservePage = () => {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const today = new Date().toISOString().split('T')[0]

  const [date, setDate] = useState(today)
  const [time, setTime] = useState('19:00')

  const mutation = useMutation({
    mutationFn: () => reserve({ date, time }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['myReservations'] })
      alert('예약이 완료되었습니다!')
      navigate('/bar')
    },
    onError: () => alert('예약에 실패했습니다. 다시 시도해주세요.'),
  })

  const inputStyle: React.CSSProperties = {
    width: '100%',
    padding: '12px 14px',
    border: '1.5px solid var(--border)',
    borderRadius: 10,
    fontSize: 15,
    fontFamily: 'inherit',
    outline: 'none',
    background: '#fff',
    color: 'var(--text)',
    marginTop: 6,
  }

  return (
    <div style={{ padding: '20px 16px' }}>
      <button
        onClick={() => navigate(-1)}
        style={{
          background: 'none',
          border: 'none',
          fontSize: 14,
          color: 'var(--text-light)',
          cursor: 'pointer',
          fontFamily: 'inherit',
          marginBottom: 16,
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
          marginBottom: 4,
        }}
      >
        🍷 혼술바 예약
      </h1>
      <p style={{ fontSize: 13, color: 'var(--text-light)', marginBottom: 24 }}>
        방문 날짜와 시간을 선택해주세요
      </p>

      <div
        style={{
          background: '#fff',
          borderRadius: 'var(--radius)',
          padding: 20,
          boxShadow: 'var(--shadow)',
          marginBottom: 20,
        }}
      >
        <div style={{ marginBottom: 16 }}>
          <label style={{ fontSize: 13, fontWeight: 600, color: 'var(--text-light)' }}>
            방문 날짜
          </label>
          <input
            type="date"
            style={inputStyle}
            value={date}
            min={today}
            onChange={(e) => setDate(e.target.value)}
          />
        </div>

        <div>
          <label style={{ fontSize: 13, fontWeight: 600, color: 'var(--text-light)' }}>
            방문 시간
          </label>
          <select
            style={inputStyle}
            value={time}
            onChange={(e) => setTime(e.target.value)}
          >
            {['17:00', '18:00', '19:00', '20:00', '21:00', '22:00'].map((t) => (
              <option key={t} value={t}>{t}</option>
            ))}
          </select>
        </div>
      </div>

      {/* Preview */}
      <div
        style={{
          background: 'linear-gradient(135deg, #fff0f4, #fff4ef)',
          border: '1.5px solid var(--primary)',
          borderRadius: 12,
          padding: '16px',
          marginBottom: 24,
        }}
      >
        <p style={{ fontSize: 13, color: 'var(--text-light)', marginBottom: 6 }}>예약 정보 확인</p>
        <p style={{ fontWeight: 700, fontSize: 16, color: 'var(--text)' }}>
          📅 {date} &nbsp; ⏰ {time}
        </p>
      </div>

      <Button
        fullWidth
        onClick={() => mutation.mutate()}
        disabled={mutation.isPending || !date || !time}
      >
        {mutation.isPending ? '예약 중...' : '예약 확정하기'}
      </Button>

      <p style={{ fontSize: 12, color: 'var(--text-light)', textAlign: 'center', marginTop: 12 }}>
        예약 취소는 방문 2시간 전까지 가능합니다.
      </p>
    </div>
  )
}

export default BarReservePage
