import { useState, useEffect, useMemo } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation } from '@tanstack/react-query'
import { getEvent, getParticipants, submitChoices } from '@/api/event'
import { Button } from '@/components/ui/button'
import EmptyState from '@/components/common/EmptyState'
import { ArrowLeft, Loader2, Clock, Check, UserX } from 'lucide-react'

function useCountdown(deadline: string | null) {
  const [remaining, setRemaining] = useState('')

  useEffect(() => {
    if (!deadline) return

    const update = () => {
      const diff = new Date(deadline).getTime() - Date.now()
      if (diff <= 0) {
        setRemaining('마감됨')
        return
      }
      const hours = Math.floor(diff / 3600000)
      const minutes = Math.floor((diff % 3600000) / 60000)
      const seconds = Math.floor((diff % 60000) / 1000)
      setRemaining(`${hours}시간 ${minutes}분 ${seconds}초`)
    }

    update()
    const interval = setInterval(update, 1000)
    return () => clearInterval(interval)
  }, [deadline])

  return remaining
}

export default function ChoicePage() {
  const { slug, eventId } = useParams<{ slug: string; eventId: string }>()
  const navigate = useNavigate()
  const [selected, setSelected] = useState<number[]>([])

  const { data: event } = useQuery({
    queryKey: ['event', slug, eventId],
    queryFn: () => getEvent(slug!, Number(eventId)),
    enabled: !!slug && !!eventId,
  })

  const { data: participants, isLoading } = useQuery({
    queryKey: ['participants', eventId],
    queryFn: () => getParticipants(Number(eventId)),
    enabled: !!eventId,
  })

  const countdown = useCountdown(event?.choiceDeadline ?? null)

  const maxChoices = event?.maxChoices ?? 3

  const toggleSelection = (number: number) => {
    setSelected((prev) => {
      if (prev.includes(number)) {
        return prev.filter((n) => n !== number)
      }
      if (prev.length >= maxChoices) return prev
      return [...prev, number]
    })
  }

  const mutation = useMutation({
    mutationFn: () => submitChoices(Number(eventId), selected),
    onSuccess: () => {
      navigate(`/cafes/${slug}/events/${eventId}/result`, { replace: true })
    },
  })

  const isDeadlinePassed = useMemo(() => {
    if (!event?.choiceDeadline) return false
    return new Date(event.choiceDeadline).getTime() < Date.now()
  }, [event?.choiceDeadline])

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <Loader2 className="w-8 h-8 text-[#FF6B6B] animate-spin" />
      </div>
    )
  }

  return (
    <div className="pb-6">
      {/* Header */}
      <div className="sticky top-0 z-10 bg-[#FFF5F5] px-4 py-3 flex items-center gap-3">
        <button
          onClick={() => navigate(-1)}
          className="w-10 h-10 flex items-center justify-center rounded-xl hover:bg-white/60 transition-colors"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>
        <h1 className="text-lg font-bold">상대방 선택</h1>
      </div>

      <div className="px-4 space-y-4">
        {/* Countdown + Selection Count */}
        <div className="flex items-center justify-between bg-white rounded-2xl p-4 shadow-sm">
          <div className="flex items-center gap-2 text-sm text-gray-600">
            <Clock className="w-4 h-4 text-[#FF6B6B]" />
            <span>{countdown || '제한 없음'}</span>
          </div>
          <div className="text-sm font-medium">
            <span className="text-[#FF6B6B]">{selected.length}</span>
            <span className="text-gray-400"> / {maxChoices} 선택</span>
          </div>
        </div>

        {/* Participants Grid */}
        {participants && participants.length > 0 ? (
          <div className="grid grid-cols-2 gap-3">
            {participants.map((p) => {
              const isSelected = selected.includes(p.number)
              return (
                <button
                  key={p.number}
                  onClick={() => toggleSelection(p.number)}
                  disabled={isDeadlinePassed}
                  className={`relative bg-white rounded-2xl p-4 text-left transition-all ${
                    isSelected
                      ? 'ring-2 ring-[#FF6B6B] shadow-md'
                      : 'shadow-sm border border-gray-50'
                  } ${isDeadlinePassed ? 'opacity-60' : 'active:scale-[0.98]'}`}
                >
                  {isSelected && (
                    <div className="absolute top-2 right-2 w-6 h-6 rounded-full bg-[#FF6B6B] flex items-center justify-center">
                      <Check className="w-3.5 h-3.5 text-white" />
                    </div>
                  )}

                  <div className="text-2xl font-bold text-[#FF6B6B] mb-1">
                    #{p.number}
                  </div>
                  <div className="text-sm text-gray-500 mb-0.5">
                    {p.gender === 'MALE' ? '남' : '여'} / {p.age}세
                  </div>
                  <div className="text-sm font-medium text-gray-800 mb-2">
                    {p.job}
                  </div>
                  {p.introduction && (
                    <p className="text-xs text-gray-400 line-clamp-2">
                      {p.introduction}
                    </p>
                  )}
                </button>
              )
            })}
          </div>
        ) : (
          <EmptyState
            icon={UserX}
            message="참가자 정보가 없습니다"
            description="아직 참가자가 공개되지 않았어요."
          />
        )}

        {/* Submit Button */}
        {participants && participants.length > 0 && (
          <Button
            onClick={() => mutation.mutate()}
            disabled={selected.length === 0 || mutation.isPending || isDeadlinePassed}
            className="w-full h-12 rounded-xl bg-[#FF6B6B] hover:bg-[#FF5252] text-white font-semibold text-base"
          >
            {mutation.isPending ? (
              <Loader2 className="w-5 h-5 animate-spin" />
            ) : isDeadlinePassed ? (
              '선택 마감됨'
            ) : (
              `선택 완료 (${selected.length}명)`
            )}
          </Button>
        )}

        {mutation.isError && (
          <p className="text-center text-sm text-red-500">
            선택 제출에 실패했습니다. 다시 시도해주세요.
          </p>
        )}
      </div>
    </div>
  )
}
