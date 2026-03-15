import { useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getMyApplications, cancelApplication } from '../api/application'
import type { Application, ApplicationStatus } from '../types'

const statusConfig: Record<ApplicationStatus, { label: string; bg: string; color: string }> = {
  PAYMENT_WAITING: { label: '결제 대기', bg: '#fff8e1', color: '#f57f17' },
  PAID: { label: '결제 완료', bg: '#e8f5e9', color: '#2e7d32' },
  APPROVED: { label: '승인됨', bg: '#e3f2fd', color: '#1565c0' },
  REJECTED: { label: '거절됨', bg: '#ffebee', color: '#b71c1c' },
  CANCELLED: { label: '취소됨', bg: '#f5f5f5', color: '#9e9e9e' },
  COMPLETED: { label: '완료됨', bg: '#f3e5f5', color: '#6a1b9a' },
}

const MyApplicationsPage = () => {
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const { data: applications = [], isLoading } = useQuery({
    queryKey: ['myApplications'],
    queryFn: getMyApplications,
  })

  const cancelMutation = useMutation({
    mutationFn: cancelApplication,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['myApplications'] }),
    onError: () => alert('취소에 실패했습니다.'),
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
          내 신청 내역
        </h1>
      </div>

      {isLoading ? (
        <div style={{ textAlign: 'center', padding: 40, color: 'var(--text-light)' }}>
          <p>불러오는 중...</p>
        </div>
      ) : applications.length === 0 ? (
        <div
          style={{
            textAlign: 'center',
            padding: '50px 20px',
            background: '#fff',
            borderRadius: 'var(--radius)',
            boxShadow: 'var(--shadow)',
          }}
        >
          <div style={{ fontSize: 48, marginBottom: 12 }}>📋</div>
          <p style={{ color: 'var(--text-light)', fontSize: 15 }}>신청 내역이 없습니다.</p>
          <button
            onClick={() => navigate('/')}
            style={{
              marginTop: 16,
              background: 'var(--gradient)',
              border: 'none',
              borderRadius: 12,
              padding: '12px 24px',
              color: '#fff',
              fontWeight: 700,
              fontSize: 14,
              cursor: 'pointer',
              fontFamily: 'inherit',
            }}
          >
            이벤트 찾아보기
          </button>
        </div>
      ) : (
        applications.map((app: Application) => {
          const st = statusConfig[app.status]
          const canCancel = app.status === 'PAYMENT_WAITING' || app.status === 'PAID'
          return (
            <div
              key={app.id}
              style={{
                background: '#fff',
                borderRadius: 'var(--radius)',
                padding: '16px',
                boxShadow: 'var(--shadow)',
                marginBottom: 10,
                border: '1px solid var(--border)',
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 10 }}>
                <div>
                  <p style={{ fontSize: 13, color: 'var(--text-light)', marginBottom: 2 }}>
                    이벤트 #{app.eventId}
                  </p>
                  <p style={{ fontSize: 12, color: 'var(--text-light)' }}>
                    신청일: {new Date(app.appliedAt).toLocaleDateString()}
                  </p>
                </div>
                <span
                  style={{
                    background: st.bg,
                    color: st.color,
                    fontSize: 12,
                    fontWeight: 700,
                    padding: '4px 10px',
                    borderRadius: 20,
                  }}
                >
                  {st.label}
                </span>
              </div>

              {app.rejectReason && (
                <p
                  style={{
                    fontSize: 13,
                    color: '#b71c1c',
                    background: '#ffebee',
                    borderRadius: 8,
                    padding: '8px 10px',
                    marginBottom: 10,
                  }}
                >
                  거절 사유: {app.rejectReason}
                </p>
              )}

              <div style={{ display: 'flex', gap: 8 }}>
                <button
                  onClick={() => navigate(`/events/${app.eventId}`)}
                  style={{
                    flex: 1,
                    padding: '9px',
                    border: '1.5px solid var(--border)',
                    borderRadius: 8,
                    background: '#fff',
                    cursor: 'pointer',
                    fontFamily: 'inherit',
                    fontSize: 13,
                    fontWeight: 600,
                    color: 'var(--text)',
                  }}
                >
                  이벤트 보기
                </button>
                {canCancel && (
                  <button
                    onClick={() => {
                      if (confirm('정말 취소하시겠습니까?')) {
                        cancelMutation.mutate(app.id)
                      }
                    }}
                    style={{
                      flex: 1,
                      padding: '9px',
                      border: '1.5px solid #ffcdd2',
                      borderRadius: 8,
                      background: '#fff',
                      cursor: 'pointer',
                      fontFamily: 'inherit',
                      fontSize: 13,
                      fontWeight: 600,
                      color: '#e53935',
                    }}
                  >
                    신청 취소
                  </button>
                )}
              </div>
            </div>
          )
        })
      )}
    </div>
  )
}

export default MyApplicationsPage
