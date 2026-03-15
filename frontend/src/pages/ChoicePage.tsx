import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation } from '@tanstack/react-query'
import { getParticipants, submitChoices } from '../api/matching'
import Button from '../components/common/Button'
import type { ParticipantInfo } from '../types'

const MAX_CHOICES = 3

const ChoicePage = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const eventId = Number(id)
  const [selected, setSelected] = useState<number[]>([])

  const { data: participants = [], isLoading } = useQuery({
    queryKey: ['participants', eventId],
    queryFn: () => getParticipants(eventId),
  })

  // Show only opposite gender (frontend assumes user's gender is stored)
  // Show all participants for now since gender filtering would need auth context
  const oppositeGender: ParticipantInfo[] = participants

  const mutation = useMutation({
    mutationFn: () => submitChoices(eventId, selected),
    onSuccess: () => navigate(`/events/${eventId}/result`),
    onError: () => alert('제출에 실패했습니다. 다시 시도해주세요.'),
  })

  const toggle = (num: number) => {
    setSelected((prev) => {
      if (prev.includes(num)) return prev.filter((n) => n !== num)
      if (prev.length >= MAX_CHOICES) return prev
      return [...prev, num]
    })
  }

  return (
    <div style={{ padding: '20px 16px' }}>
      {/* Header */}
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
          마음에 드는 분을 선택해주세요
        </h1>
        <p style={{ fontSize: 13, color: 'var(--text-light)', marginTop: 6 }}>
          최대 {MAX_CHOICES}명까지 선택할 수 있어요
        </p>
      </div>

      {/* Selection counter */}
      <div
        style={{
          background: 'var(--gradient)',
          borderRadius: 12,
          padding: '14px 18px',
          marginBottom: 20,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          color: '#fff',
        }}
      >
        <span style={{ fontSize: 14, fontWeight: 600 }}>선택한 인원</span>
        <span style={{ fontSize: 20, fontWeight: 800 }}>
          {selected.length} / {MAX_CHOICES}
        </span>
      </div>

      {isLoading ? (
        <div style={{ textAlign: 'center', padding: 40, color: 'var(--text-light)' }}>
          <div style={{ fontSize: 32, marginBottom: 8 }}>💭</div>
          <p>참가자 목록을 불러오는 중...</p>
        </div>
      ) : oppositeGender.length === 0 ? (
        <div
          style={{
            textAlign: 'center',
            padding: 40,
            background: '#fff',
            borderRadius: 'var(--radius)',
            boxShadow: 'var(--shadow)',
          }}
        >
          <div style={{ fontSize: 40, marginBottom: 10 }}>🤷</div>
          <p style={{ color: 'var(--text-light)' }}>참가자 정보가 없습니다.</p>
        </div>
      ) : (
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(3, 1fr)',
            gap: 10,
            marginBottom: 24,
          }}
        >
          {oppositeGender.map((p) => {
            const isSelected = selected.includes(p.number)
            return (
              <button
                key={p.number}
                onClick={() => toggle(p.number)}
                style={{
                  background: isSelected ? '#fff' : '#fff',
                  border: isSelected ? '2.5px solid transparent' : '2px solid var(--border)',
                  borderRadius: 14,
                  padding: '20px 10px',
                  cursor: 'pointer',
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  gap: 8,
                  fontFamily: 'inherit',
                  position: 'relative',
                  backgroundClip: isSelected ? 'padding-box' : 'border-box',
                  outline: isSelected ? '2.5px solid var(--primary)' : 'none',
                  boxShadow: isSelected ? '0 4px 16px rgba(255,107,138,0.25)' : 'var(--shadow)',
                  transition: 'all 0.2s',
                }}
              >
                {isSelected && (
                  <div
                    style={{
                      position: 'absolute',
                      top: 8,
                      right: 8,
                      width: 20,
                      height: 20,
                      background: 'var(--gradient)',
                      borderRadius: '50%',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                    }}
                  >
                    <span style={{ color: '#fff', fontSize: 11, fontWeight: 700 }}>✓</span>
                  </div>
                )}
                <div
                  style={{
                    width: 56,
                    height: 56,
                    background: p.gender === 'MALE'
                      ? 'linear-gradient(135deg, #4a90d9, #74b0e8)'
                      : 'var(--gradient)',
                    borderRadius: '50%',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontSize: 28,
                  }}
                >
                  {p.gender === 'MALE' ? '👨' : '👩'}
                </div>
                <span
                  style={{
                    fontSize: 18,
                    fontWeight: 800,
                    background: isSelected ? 'var(--gradient)' : 'none',
                    WebkitBackgroundClip: isSelected ? 'text' : 'unset',
                    WebkitTextFillColor: isSelected ? 'transparent' : 'var(--text)',
                  }}
                >
                  {p.number}번
                </span>
              </button>
            )
          })}
        </div>
      )}

      <Button
        fullWidth
        onClick={() => mutation.mutate()}
        disabled={selected.length === 0 || mutation.isPending}
      >
        {mutation.isPending ? '제출 중...' : `💕 ${selected.length}명 선택 완료`}
      </Button>
    </div>
  )
}

export default ChoicePage
