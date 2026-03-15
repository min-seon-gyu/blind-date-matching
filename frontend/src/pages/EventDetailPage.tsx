import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation } from '@tanstack/react-query'
import { useEvent } from '../hooks/useEvents'
import { apply, getMyApplications } from '../api/application'
import CapacityBar from '../components/event/CapacityBar'
import Button from '../components/common/Button'

const statusLabel: Record<string, string> = {
  PAYMENT_WAITING: '결제 대기',
  PAID: '결제 완료',
  APPROVED: '승인됨',
  REJECTED: '거절됨',
  CANCELLED: '취소됨',
  COMPLETED: '완료됨',
}

const EventDetailPage = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const eventId = Number(id)

  const { data: event, isLoading } = useEvent(eventId)
  const { data: applications = [] } = useQuery({
    queryKey: ['myApplications'],
    queryFn: getMyApplications,
  })

  const myApp = applications.find((a) => a.eventId === eventId)

  const mutation = useMutation({
    mutationFn: () => apply(eventId),
    onSuccess: () => {
      alert('신청이 완료되었습니다! 결제를 진행해주세요.')
    },
    onError: () => {
      alert('신청에 실패했습니다. 다시 시도해주세요.')
    },
  })

  if (isLoading) {
    return (
      <div style={{ textAlign: 'center', padding: 60, color: 'var(--text-light)' }}>
        <div style={{ fontSize: 40 }}>💕</div>
        <p style={{ marginTop: 10 }}>불러오는 중...</p>
      </div>
    )
  }

  if (!event) {
    return (
      <div style={{ textAlign: 'center', padding: 60, color: 'var(--text-light)' }}>
        <p>이벤트를 찾을 수 없습니다.</p>
      </div>
    )
  }

  const dateObj = new Date(event.date)
  const formattedDate = `${dateObj.getFullYear()}년 ${dateObj.getMonth() + 1}월 ${dateObj.getDate()}일`
  const maleAvailable = event.maleCapacity - event.currentMaleCount > 0
  const femaleAvailable = event.femaleCapacity - event.currentFemaleCount > 0
  const canApply = event.status === 'OPEN' && (maleAvailable || femaleAvailable) && !myApp

  return (
    <div>
      {/* Hero */}
      <div
        style={{
          background: 'var(--gradient)',
          padding: '32px 20px 24px',
          color: '#fff',
          position: 'relative',
        }}
      >
        <button
          onClick={() => navigate(-1)}
          style={{
            background: 'rgba(255,255,255,0.2)',
            border: 'none',
            borderRadius: 8,
            padding: '6px 10px',
            color: '#fff',
            cursor: 'pointer',
            marginBottom: 16,
            fontFamily: 'inherit',
            fontSize: 14,
          }}
        >
          ← 뒤로
        </button>
        <div
          style={{
            display: 'inline-block',
            background: event.status === 'OPEN' ? 'rgba(255,255,255,0.25)' : 'rgba(0,0,0,0.2)',
            padding: '3px 10px',
            borderRadius: 20,
            fontSize: 12,
            fontWeight: 600,
            marginBottom: 10,
          }}
        >
          {event.status === 'OPEN' ? '✨ 모집중' : event.status === 'CLOSED' ? '🔒 마감' : '✅ 완료'}
        </div>
        <h1 style={{ fontSize: 24, fontWeight: 800, letterSpacing: '-0.5px', marginBottom: 8 }}>
          {event.title}
        </h1>
        <p style={{ fontSize: 14, opacity: 0.9 }}>
          📅 {formattedDate} &nbsp; ⏰ {event.time}
        </p>
      </div>

      <div style={{ padding: '20px' }}>
        {/* My application status */}
        {myApp && (
          <div
            style={{
              background: '#fff0f4',
              border: '1.5px solid var(--primary)',
              borderRadius: 12,
              padding: '14px 16px',
              marginBottom: 16,
              display: 'flex',
              alignItems: 'center',
              gap: 10,
            }}
          >
            <span style={{ fontSize: 20 }}>📋</span>
            <div>
              <p style={{ fontWeight: 700, fontSize: 14, color: 'var(--primary)' }}>신청 완료</p>
              <p style={{ fontSize: 13, color: 'var(--text-light)' }}>
                상태: {statusLabel[myApp.status] ?? myApp.status}
              </p>
            </div>
          </div>
        )}

        {/* Info card */}
        <div
          style={{
            background: '#fff',
            borderRadius: 'var(--radius)',
            padding: 20,
            boxShadow: 'var(--shadow)',
            marginBottom: 14,
          }}
        >
          <h2 style={{ fontSize: 15, fontWeight: 700, marginBottom: 14, color: 'var(--text)' }}>
            이벤트 정보
          </h2>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            {[
              { label: '💰 참가비', value: `${event.price.toLocaleString()}원` },
              { label: '🎯 나이 제한', value: event.minAge && event.maxAge ? `${event.minAge}~${event.maxAge}세` : '제한 없음' },
              { label: '📅 선택 마감', value: new Date(event.choiceDeadline).toLocaleDateString() },
              { label: '📊 상태', value: event.status === 'OPEN' ? '모집중' : '마감' },
            ].map(({ label, value }) => (
              <div
                key={label}
                style={{
                  background: '#fafafa',
                  borderRadius: 10,
                  padding: '10px 12px',
                }}
              >
                <p style={{ fontSize: 11, color: 'var(--text-light)', marginBottom: 3 }}>{label}</p>
                <p style={{ fontSize: 14, fontWeight: 600, color: 'var(--text)' }}>{value}</p>
              </div>
            ))}
          </div>
        </div>

        {/* Capacity */}
        <div
          style={{
            background: '#fff',
            borderRadius: 'var(--radius)',
            padding: 20,
            boxShadow: 'var(--shadow)',
            marginBottom: 14,
          }}
        >
          <h2 style={{ fontSize: 15, fontWeight: 700, marginBottom: 14, color: 'var(--text)' }}>
            참가 현황
          </h2>
          <CapacityBar
            current={event.currentMaleCount}
            total={event.maleCapacity}
            gender="MALE"
          />
          <CapacityBar
            current={event.currentFemaleCount}
            total={event.femaleCapacity}
            gender="FEMALE"
          />
        </div>

        {/* Description */}
        {event.description && (
          <div
            style={{
              background: '#fff',
              borderRadius: 'var(--radius)',
              padding: 20,
              boxShadow: 'var(--shadow)',
              marginBottom: 20,
            }}
          >
            <h2 style={{ fontSize: 15, fontWeight: 700, marginBottom: 10, color: 'var(--text)' }}>
              이벤트 소개
            </h2>
            <p style={{ fontSize: 14, color: 'var(--text-light)', lineHeight: 1.7 }}>
              {event.description}
            </p>
          </div>
        )}

        {/* Action */}
        {canApply && (
          <Button
            fullWidth
            onClick={() => mutation.mutate()}
            disabled={mutation.isPending}
          >
            {mutation.isPending ? '신청 중...' : '💕 신청하기'}
          </Button>
        )}

        {!canApply && !myApp && (
          <div
            style={{
              textAlign: 'center',
              padding: '16px',
              background: '#f9f9f9',
              borderRadius: 12,
              color: 'var(--text-light)',
              fontSize: 14,
            }}
          >
            {event.status !== 'OPEN' ? '이 이벤트는 마감되었습니다.' : '참가 인원이 마감되었습니다.'}
          </div>
        )}
      </div>
    </div>
  )
}

export default EventDetailPage
