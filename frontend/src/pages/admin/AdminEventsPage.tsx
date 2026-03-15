import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getEventsByMonth, createEvent, deleteEvent } from '../../api/event'
import Button from '../../components/common/Button'
import type { BlindDateEvent } from '../../types'

interface EventForm {
  title: string
  date: string
  time: string
  maleCapacity: string
  femaleCapacity: string
  price: string
  description: string
  choiceDeadline: string
  minAge: string
  maxAge: string
}

const initForm: EventForm = {
  title: '', date: '', time: '', maleCapacity: '', femaleCapacity: '',
  price: '', description: '', choiceDeadline: '', minAge: '', maxAge: '',
}

const AdminEventsPage = () => {
  const navigate = useNavigate()
  const qc = useQueryClient()
  const today = new Date()
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState<EventForm>(initForm)

  const { data: events = [], isLoading } = useQuery({
    queryKey: ['events', today.getFullYear(), today.getMonth() + 1],
    queryFn: () => getEventsByMonth(today.getFullYear(), today.getMonth() + 1),
  })

  const createMutation = useMutation({
    mutationFn: () => createEvent({
      title: form.title,
      date: form.date,
      time: form.time,
      maleCapacity: parseInt(form.maleCapacity),
      femaleCapacity: parseInt(form.femaleCapacity),
      price: parseInt(form.price),
      description: form.description,
      choiceDeadline: form.choiceDeadline,
      minAge: form.minAge ? parseInt(form.minAge) : null,
      maxAge: form.maxAge ? parseInt(form.maxAge) : null,
    }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['events'] })
      setShowForm(false)
      setForm(initForm)
    },
    onError: () => alert('이벤트 생성에 실패했습니다.'),
  })

  const deleteMutation = useMutation({
    mutationFn: deleteEvent,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['events'] }),
    onError: () => alert('삭제에 실패했습니다.'),
  })

  const upd = (k: keyof EventForm, v: string) => setForm((p) => ({ ...p, [k]: v }))

  const inputStyle: React.CSSProperties = {
    width: '100%',
    padding: '10px 12px',
    border: '1.5px solid #e8e0f0',
    borderRadius: 8,
    fontSize: 14,
    fontFamily: 'inherit',
    outline: 'none',
    background: '#fff',
    color: 'var(--text)',
    marginTop: 4,
  }

  const adminCardStyle: React.CSSProperties = {
    background: '#fff',
    borderRadius: 'var(--radius)',
    padding: '14px 16px',
    boxShadow: '0 2px 8px rgba(99,102,241,0.1)',
    border: '1px solid #e8e0f0',
    marginBottom: 8,
  }

  const statusColors: Record<string, string> = {
    OPEN: '#2e7d32', CLOSED: '#e65100', COMPLETED: '#6a1b9a',
  }

  return (
    <div style={{ padding: '20px 16px' }}>
      {/* Admin header */}
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
          <h1 style={{ fontSize: 20, fontWeight: 800, letterSpacing: '-0.4px' }}>⚙️ 이벤트 관리</h1>
          <p style={{ fontSize: 12, opacity: 0.8, marginTop: 2 }}>이번 달 소개팅 이벤트</p>
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

      <Button variant="admin" fullWidth onClick={() => setShowForm(!showForm)}>
        {showForm ? '✕ 취소' : '+ 새 이벤트 만들기'}
      </Button>

      {/* Create form */}
      {showForm && (
        <div
          style={{
            background: '#fff',
            borderRadius: 'var(--radius)',
            padding: 18,
            boxShadow: 'var(--shadow)',
            marginTop: 14,
            border: '1.5px solid #e8e0f0',
          }}
        >
          <h2 style={{ fontSize: 15, fontWeight: 700, marginBottom: 14, color: '#6366f1' }}>
            새 이벤트
          </h2>
          <div style={{ marginBottom: 10 }}>
            <label style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-light)' }}>제목</label>
            <input style={inputStyle} placeholder="이벤트 제목" value={form.title} onChange={(e) => upd('title', e.target.value)} />
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10, marginBottom: 10 }}>
            <div>
              <label style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-light)' }}>날짜</label>
              <input style={inputStyle} type="date" value={form.date} onChange={(e) => upd('date', e.target.value)} />
            </div>
            <div>
              <label style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-light)' }}>시간</label>
              <input style={inputStyle} type="time" value={form.time} onChange={(e) => upd('time', e.target.value)} />
            </div>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10, marginBottom: 10 }}>
            <div>
              <label style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-light)' }}>남성 인원</label>
              <input style={inputStyle} type="number" placeholder="5" value={form.maleCapacity} onChange={(e) => upd('maleCapacity', e.target.value)} />
            </div>
            <div>
              <label style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-light)' }}>여성 인원</label>
              <input style={inputStyle} type="number" placeholder="5" value={form.femaleCapacity} onChange={(e) => upd('femaleCapacity', e.target.value)} />
            </div>
          </div>
          <div style={{ marginBottom: 10 }}>
            <label style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-light)' }}>참가비 (원)</label>
            <input style={inputStyle} type="number" placeholder="30000" value={form.price} onChange={(e) => upd('price', e.target.value)} />
          </div>
          <div style={{ marginBottom: 10 }}>
            <label style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-light)' }}>선택 마감일</label>
            <input style={inputStyle} type="datetime-local" value={form.choiceDeadline} onChange={(e) => upd('choiceDeadline', e.target.value)} />
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10, marginBottom: 10 }}>
            <div>
              <label style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-light)' }}>최소 나이</label>
              <input style={inputStyle} type="number" placeholder="25" value={form.minAge} onChange={(e) => upd('minAge', e.target.value)} />
            </div>
            <div>
              <label style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-light)' }}>최대 나이</label>
              <input style={inputStyle} type="number" placeholder="35" value={form.maxAge} onChange={(e) => upd('maxAge', e.target.value)} />
            </div>
          </div>
          <div style={{ marginBottom: 14 }}>
            <label style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-light)' }}>설명</label>
            <textarea
              style={{ ...inputStyle, height: 80, resize: 'none' }}
              placeholder="이벤트 설명"
              value={form.description}
              onChange={(e) => upd('description', e.target.value)}
            />
          </div>
          <Button
            variant="admin"
            fullWidth
            onClick={() => createMutation.mutate()}
            disabled={createMutation.isPending || !form.title || !form.date}
          >
            {createMutation.isPending ? '생성 중...' : '이벤트 생성'}
          </Button>
        </div>
      )}

      {/* Event list */}
      <div style={{ marginTop: 20 }}>
        <h2 style={{ fontSize: 15, fontWeight: 700, marginBottom: 12, color: 'var(--text)' }}>
          이벤트 목록 ({events.length}개)
        </h2>

        {isLoading ? (
          <p style={{ color: 'var(--text-light)', textAlign: 'center', padding: 20 }}>불러오는 중...</p>
        ) : events.length === 0 ? (
          <div style={{ ...adminCardStyle, textAlign: 'center', padding: 30 }}>
            <p style={{ color: 'var(--text-light)' }}>이벤트가 없습니다.</p>
          </div>
        ) : (
          events.map((event: BlindDateEvent) => (
            <div key={event.id} style={adminCardStyle}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 }}>
                <div>
                  <span
                    style={{
                      fontSize: 11,
                      fontWeight: 700,
                      color: statusColors[event.status] ?? '#666',
                      background: `${statusColors[event.status]}18`,
                      padding: '2px 8px',
                      borderRadius: 10,
                      marginBottom: 4,
                      display: 'inline-block',
                    }}
                  >
                    {event.status === 'OPEN' ? '모집중' : event.status === 'CLOSED' ? '마감' : '완료'}
                  </span>
                  <h3 style={{ fontSize: 15, fontWeight: 700, color: 'var(--text)', letterSpacing: '-0.3px' }}>
                    {event.title}
                  </h3>
                </div>
                <button
                  onClick={() => {
                    if (confirm('이벤트를 삭제하시겠습니까?')) {
                      deleteMutation.mutate(event.id)
                    }
                  }}
                  style={{
                    background: '#ffebee',
                    border: 'none',
                    borderRadius: 6,
                    padding: '4px 8px',
                    color: '#e53935',
                    cursor: 'pointer',
                    fontSize: 12,
                    fontFamily: 'inherit',
                  }}
                >
                  삭제
                </button>
              </div>
              <p style={{ fontSize: 12, color: 'var(--text-light)' }}>
                📅 {event.date} {event.time} &nbsp;·&nbsp;
                👨 {event.currentMaleCount}/{event.maleCapacity} &nbsp;·&nbsp;
                👩 {event.currentFemaleCount}/{event.femaleCapacity} &nbsp;·&nbsp;
                💰 {event.price.toLocaleString()}원
              </p>
            </div>
          ))
        )}
      </div>
    </div>
  )
}

export default AdminEventsPage
