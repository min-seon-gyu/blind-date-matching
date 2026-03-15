import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getBarStatus, openBar, closeBar, checkIn, checkOut, getVisitors } from '../../api/bar'
import Button from '../../components/common/Button'
import type { BarStatus } from '../../types'

const AdminBarPage = () => {
  const navigate = useNavigate()
  const qc = useQueryClient()
  const [checkInGender, setCheckInGender] = useState<'MALE' | 'FEMALE'>('MALE')
  const [checkInMemberId, setCheckInMemberId] = useState('')

  const { data: barStatus } = useQuery<BarStatus>({
    queryKey: ['barStatus'],
    queryFn: getBarStatus,
    refetchInterval: 10000,
  })

  const { data: visitors = [] } = useQuery({
    queryKey: ['visitors'],
    queryFn: getVisitors,
    refetchInterval: 10000,
  })

  const openMutation = useMutation({
    mutationFn: openBar,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['barStatus'] }),
    onError: () => alert('실패'),
  })

  const closeMutation = useMutation({
    mutationFn: closeBar,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['barStatus'] }),
    onError: () => alert('실패'),
  })

  const checkInMutation = useMutation({
    mutationFn: () => checkIn({
      gender: checkInGender,
      memberId: checkInMemberId ? Number(checkInMemberId) : undefined,
    }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['barStatus'] })
      qc.invalidateQueries({ queryKey: ['visitors'] })
      setCheckInMemberId('')
    },
    onError: () => alert('체크인 실패'),
  })

  const checkOutMutation = useMutation({
    mutationFn: checkOut,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['barStatus'] })
      qc.invalidateQueries({ queryKey: ['visitors'] })
    },
    onError: () => alert('체크아웃 실패'),
  })

  const inputStyle: React.CSSProperties = {
    width: '100%',
    padding: '10px 12px',
    border: '1.5px solid #e8e0f0',
    borderRadius: 8,
    fontSize: 14,
    fontFamily: 'inherit',
    outline: 'none',
    background: '#fff',
    marginTop: 4,
  }

  const sectionCard: React.CSSProperties = {
    background: '#fff',
    borderRadius: 'var(--radius)',
    padding: '16px',
    boxShadow: '0 2px 8px rgba(99,102,241,0.1)',
    border: '1px solid #e8e0f0',
    marginBottom: 14,
  }

  return (
    <div style={{ padding: '20px 16px' }}>
      <div
        style={{
          background: 'var(--admin-gradient)',
          borderRadius: 'var(--radius)',
          padding: '18px',
          marginBottom: 20,
          color: '#fff',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
        }}
      >
        <div>
          <h1 style={{ fontSize: 20, fontWeight: 800 }}>⚙️ 바 관리</h1>
          <p style={{ fontSize: 12, opacity: 0.8, marginTop: 2 }}>실시간 현황 및 체크인 관리</p>
        </div>
        <button
          onClick={() => navigate(-1)}
          style={{
            background: 'rgba(255,255,255,0.2)',
            border: 'none',
            borderRadius: 8,
            padding: '6px 12px',
            color: '#fff',
            cursor: 'pointer',
            fontFamily: 'inherit',
            fontSize: 13,
          }}
        >
          뒤로
        </button>
      </div>

      {/* Status + open/close */}
      <div style={sectionCard}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 14 }}>
          <h2 style={{ fontSize: 15, fontWeight: 700, color: '#6366f1' }}>영업 상태</h2>
          <span
            style={{
              fontSize: 13,
              fontWeight: 700,
              padding: '4px 12px',
              borderRadius: 20,
              background: barStatus?.isOpen ? '#e8f5e9' : '#ffebee',
              color: barStatus?.isOpen ? '#2e7d32' : '#b71c1c',
            }}
          >
            {barStatus?.isOpen ? '🟢 영업중' : '🔴 마감'}
          </span>
        </div>

        {barStatus && (
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 8, marginBottom: 14 }}>
            {[
              { label: '👨 남성', value: barStatus.currentMaleCount },
              { label: '👩 여성', value: barStatus.currentFemaleCount },
              { label: '💺 잔여', value: barStatus.remainingSeats },
            ].map(({ label, value }) => (
              <div
                key={label}
                style={{
                  background: '#f5f3ff',
                  borderRadius: 8,
                  padding: '10px',
                  textAlign: 'center',
                }}
              >
                <p style={{ fontSize: 11, color: '#6366f1', marginBottom: 2 }}>{label}</p>
                <p style={{ fontSize: 24, fontWeight: 800, color: '#6366f1' }}>{value}</p>
              </div>
            ))}
          </div>
        )}

        <div style={{ display: 'flex', gap: 10 }}>
          <button
            onClick={() => openMutation.mutate()}
            disabled={barStatus?.isOpen || openMutation.isPending}
            style={{
              flex: 1,
              padding: '10px',
              background: barStatus?.isOpen ? '#f5f5f5' : '#e8f5e9',
              border: '1.5px solid #4caf50',
              borderRadius: 8,
              color: barStatus?.isOpen ? '#9e9e9e' : '#2e7d32',
              fontWeight: 700,
              fontSize: 14,
              cursor: barStatus?.isOpen ? 'not-allowed' : 'pointer',
              fontFamily: 'inherit',
              opacity: barStatus?.isOpen ? 0.5 : 1,
            }}
          >
            🟢 오픈
          </button>
          <button
            onClick={() => closeMutation.mutate()}
            disabled={!barStatus?.isOpen || closeMutation.isPending}
            style={{
              flex: 1,
              padding: '10px',
              background: !barStatus?.isOpen ? '#f5f5f5' : '#ffebee',
              border: '1.5px solid #ef5350',
              borderRadius: 8,
              color: !barStatus?.isOpen ? '#9e9e9e' : '#b71c1c',
              fontWeight: 700,
              fontSize: 14,
              cursor: !barStatus?.isOpen ? 'not-allowed' : 'pointer',
              fontFamily: 'inherit',
              opacity: !barStatus?.isOpen ? 0.5 : 1,
            }}
          >
            🔴 마감
          </button>
        </div>
      </div>

      {/* Check-in form */}
      <div style={sectionCard}>
        <h2 style={{ fontSize: 15, fontWeight: 700, color: '#6366f1', marginBottom: 12 }}>체크인</h2>

        <div style={{ marginBottom: 10 }}>
          <label style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-light)' }}>성별</label>
          <div style={{ display: 'flex', gap: 8, marginTop: 6 }}>
            {(['MALE', 'FEMALE'] as const).map((g) => (
              <button
                key={g}
                onClick={() => setCheckInGender(g)}
                style={{
                  flex: 1,
                  padding: '9px',
                  border: `2px solid ${checkInGender === g ? '#6366f1' : '#e8e0f0'}`,
                  borderRadius: 8,
                  background: checkInGender === g ? '#ede9fe' : '#fff',
                  color: checkInGender === g ? '#6366f1' : 'var(--text-light)',
                  fontWeight: 700,
                  fontSize: 13,
                  cursor: 'pointer',
                  fontFamily: 'inherit',
                }}
              >
                {g === 'MALE' ? '👨 남성' : '👩 여성'}
              </button>
            ))}
          </div>
        </div>

        <div style={{ marginBottom: 12 }}>
          <label style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-light)' }}>회원 ID (선택)</label>
          <input
            style={inputStyle}
            type="number"
            placeholder="회원 ID (없으면 비워두세요)"
            value={checkInMemberId}
            onChange={(e) => setCheckInMemberId(e.target.value)}
          />
        </div>

        <Button variant="admin" fullWidth onClick={() => checkInMutation.mutate()} disabled={checkInMutation.isPending}>
          {checkInMutation.isPending ? '처리 중...' : '체크인'}
        </Button>
      </div>

      {/* Visitors */}
      <div style={sectionCard}>
        <h2 style={{ fontSize: 15, fontWeight: 700, color: '#6366f1', marginBottom: 12 }}>
          현재 방문자 ({Array.isArray(visitors) ? visitors.length : 0}명)
        </h2>

        {!Array.isArray(visitors) || visitors.length === 0 ? (
          <p style={{ color: 'var(--text-light)', textAlign: 'center', padding: '12px 0' }}>
            방문자가 없습니다.
          </p>
        ) : (
          visitors.map((v: { id: number; gender: string; memberId?: number; checkInTime: string }) => (
            <div
              key={v.id}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: 10,
                padding: '10px 0',
                borderBottom: '1px solid var(--border)',
              }}
            >
              <span style={{ fontSize: 20 }}>{v.gender === 'MALE' ? '👨' : '👩'}</span>
              <div style={{ flex: 1 }}>
                <p style={{ fontWeight: 600, fontSize: 13, color: 'var(--text)' }}>
                  {v.memberId ? `회원 #${v.memberId}` : '비회원'}
                </p>
                <p style={{ fontSize: 11, color: 'var(--text-light)' }}>
                  입장: {new Date(v.checkInTime).toLocaleTimeString()}
                </p>
              </div>
              <button
                onClick={() => checkOutMutation.mutate(v.id)}
                style={{
                  background: '#ffebee',
                  border: 'none',
                  borderRadius: 6,
                  padding: '5px 10px',
                  color: '#b71c1c',
                  cursor: 'pointer',
                  fontFamily: 'inherit',
                  fontSize: 12,
                  fontWeight: 600,
                }}
              >
                체크아웃
              </button>
            </div>
          ))
        )}
      </div>
    </div>
  )
}

export default AdminBarPage
