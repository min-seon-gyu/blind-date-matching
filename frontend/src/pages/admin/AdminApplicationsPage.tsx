import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getApplicationsByEvent, approveApplication, rejectApplication } from '../../api/application'
import { getEventsByMonth } from '../../api/event'
import type { Application, ApplicationStatus } from '../../types'

const statusConfig: Record<ApplicationStatus, { label: string; color: string }> = {
  PAYMENT_WAITING: { label: '결제 대기', color: '#f57f17' },
  PAID: { label: '결제 완료', color: '#2e7d32' },
  APPROVED: { label: '승인됨', color: '#1565c0' },
  REJECTED: { label: '거절됨', color: '#b71c1c' },
  CANCELLED: { label: '취소됨', color: '#9e9e9e' },
  COMPLETED: { label: '완료됨', color: '#6a1b9a' },
}

const AdminApplicationsPage = () => {
  const navigate = useNavigate()
  const qc = useQueryClient()
  const today = new Date()
  const [selectedEventId, setSelectedEventId] = useState<number | null>(null)
  const [_rejectReason, setRejectReason] = useState('')

  const { data: events = [] } = useQuery({
    queryKey: ['events', today.getFullYear(), today.getMonth() + 1],
    queryFn: () => getEventsByMonth(today.getFullYear(), today.getMonth() + 1),
  })

  const { data: applications = [], isLoading } = useQuery({
    queryKey: ['adminApplications', selectedEventId],
    queryFn: () => getApplicationsByEvent(selectedEventId!),
    enabled: !!selectedEventId,
  })

  const approveMutation = useMutation({
    mutationFn: approveApplication,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['adminApplications'] }),
    onError: () => alert('승인 실패'),
  })

  const rejectMutation = useMutation({
    mutationFn: ({ id, reason }: { id: number; reason: string }) => rejectApplication(id, reason),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['adminApplications'] })
      setRejectReason('')
    },
    onError: () => alert('거절 실패'),
  })

  const cardStyle: React.CSSProperties = {
    background: '#fff',
    borderRadius: 12,
    padding: '14px 16px',
    boxShadow: '0 2px 8px rgba(99,102,241,0.1)',
    border: '1px solid #e8e0f0',
    marginBottom: 8,
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
          <h1 style={{ fontSize: 20, fontWeight: 800 }}>⚙️ 신청 관리</h1>
          <p style={{ fontSize: 12, opacity: 0.8, marginTop: 2 }}>이벤트별 신청 현황</p>
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

      {/* Event selector */}
      <div style={{ marginBottom: 16 }}>
        <label style={{ fontSize: 13, fontWeight: 600, color: 'var(--text-light)', display: 'block', marginBottom: 6 }}>
          이벤트 선택
        </label>
        <select
          style={{
            width: '100%',
            padding: '12px 14px',
            border: '1.5px solid #e8e0f0',
            borderRadius: 10,
            fontSize: 14,
            fontFamily: 'inherit',
            outline: 'none',
            background: '#fff',
          }}
          value={selectedEventId ?? ''}
          onChange={(e) => setSelectedEventId(e.target.value ? Number(e.target.value) : null)}
        >
          <option value="">이벤트를 선택하세요</option>
          {events.map((ev: { id: number; title: string; date: string }) => (
            <option key={ev.id} value={ev.id}>{ev.date} - {ev.title}</option>
          ))}
        </select>
      </div>

      {!selectedEventId ? (
        <div style={{ ...cardStyle, textAlign: 'center', padding: 30 }}>
          <p style={{ color: 'var(--text-light)' }}>이벤트를 선택하면 신청 목록이 표시됩니다.</p>
        </div>
      ) : isLoading ? (
        <p style={{ textAlign: 'center', color: 'var(--text-light)', padding: 20 }}>불러오는 중...</p>
      ) : applications.length === 0 ? (
        <div style={{ ...cardStyle, textAlign: 'center', padding: 30 }}>
          <p style={{ color: 'var(--text-light)' }}>신청 내역이 없습니다.</p>
        </div>
      ) : (
        applications.map((app: Application) => {
          const st = statusConfig[app.status]
          return (
            <div key={app.id} style={cardStyle}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
                <div>
                  <p style={{ fontWeight: 700, fontSize: 14, color: 'var(--text)' }}>
                    회원 #{app.memberId}
                  </p>
                  <p style={{ fontSize: 12, color: 'var(--text-light)', marginTop: 2 }}>
                    신청일: {new Date(app.appliedAt).toLocaleDateString()}
                  </p>
                </div>
                <span
                  style={{
                    fontSize: 11,
                    fontWeight: 700,
                    color: st.color,
                    background: `${st.color}15`,
                    padding: '3px 8px',
                    borderRadius: 10,
                  }}
                >
                  {st.label}
                </span>
              </div>

              {(app.status === 'PAID') && (
                <div style={{ display: 'flex', gap: 8 }}>
                  <button
                    onClick={() => approveMutation.mutate(app.id)}
                    style={{
                      flex: 1,
                      padding: '8px',
                      background: '#e8f5e9',
                      border: '1.5px solid #4caf50',
                      borderRadius: 8,
                      color: '#2e7d32',
                      fontWeight: 700,
                      fontSize: 13,
                      cursor: 'pointer',
                      fontFamily: 'inherit',
                    }}
                  >
                    ✅ 승인
                  </button>
                  <button
                    onClick={() => {
                      const reason = prompt('거절 사유를 입력하세요:')
                      if (reason) rejectMutation.mutate({ id: app.id, reason })
                    }}
                    style={{
                      flex: 1,
                      padding: '8px',
                      background: '#ffebee',
                      border: '1.5px solid #ef5350',
                      borderRadius: 8,
                      color: '#b71c1c',
                      fontWeight: 700,
                      fontSize: 13,
                      cursor: 'pointer',
                      fontFamily: 'inherit',
                    }}
                  >
                    ❌ 거절
                  </button>
                </div>
              )}
            </div>
          )
        })
      )}
    </div>
  )
}

export default AdminApplicationsPage
