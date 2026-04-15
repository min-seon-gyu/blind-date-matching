import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getCafe, getEvent, applyEvent, cancelApplication } from '@/api/event'
import { getMyApplications } from '@/api/participant'
import KakaoMap from '@/components/common/KakaoMap'
import { Button } from '@/components/ui/button'
import {
  ArrowLeft, Calendar, Clock, MapPin, Users, Loader2, Navigation, Tag,
} from 'lucide-react'

export default function EventDetailPage() {
  const { slug, eventId } = useParams<{ slug: string; eventId: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const { data: cafe } = useQuery({
    queryKey: ['cafe', slug],
    queryFn: () => getCafe(slug!),
    enabled: !!slug,
  })

  const { data: event, isLoading } = useQuery({
    queryKey: ['event', slug, eventId],
    queryFn: () => getEvent(slug!, Number(eventId)),
    enabled: !!slug && !!eventId,
  })

  const { data: applications } = useQuery({
    queryKey: ['myApplications'],
    queryFn: getMyApplications,
  })

  const myApplication = applications?.find(
    (a) => a.eventId === Number(eventId)
  )

  const applyMutation = useMutation({
    mutationFn: () => applyEvent(Number(eventId)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['myApplications'] })
      queryClient.invalidateQueries({ queryKey: ['event', slug, eventId] })
    },
  })

  const cancelMutation = useMutation({
    mutationFn: () => cancelApplication(Number(eventId)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['myApplications'] })
      queryClient.invalidateQueries({ queryKey: ['event', slug, eventId] })
    },
  })

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('ko-KR').format(price)
  }

  const handleRoute = () => {
    if (!cafe?.latitude || !cafe?.longitude) return
    const url = `https://map.kakao.com/link/to/${encodeURIComponent(cafe.name)},${cafe.latitude},${cafe.longitude}`
    window.open(url, '_blank')
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <Loader2 className="w-8 h-8 text-[#FF6B6B] animate-spin" />
      </div>
    )
  }

  if (!event) {
    return (
      <div className="flex items-center justify-center min-h-[60vh] text-gray-500">
        이벤트를 찾을 수 없습니다
      </div>
    )
  }

  const statusLabel =
    event.status === 'OPEN'
      ? '🔥 모집 중'
      : event.status === 'CLOSED'
      ? '마감'
      : '완료'

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
        <h1 className="text-lg font-bold truncate">{event.title}</h1>
      </div>

      {/* Event Info */}
      <div className="px-4 space-y-5">
        {/* Status + Title */}
        <div className="bg-white rounded-2xl p-5 shadow-sm">
          <div className="flex items-center justify-between mb-3">
            <span
              className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold ${
                event.status === 'OPEN'
                  ? 'bg-red-50 text-[#FF6B6B]'
                  : event.status === 'CLOSED'
                  ? 'bg-gray-100 text-gray-500'
                  : 'bg-green-50 text-green-600'
              }`}
            >
              {statusLabel}
            </span>
            <span className="text-lg font-bold text-[#FF6B6B]">
              {formatPrice(event.price)}원
            </span>
          </div>

          <h2 className="text-xl font-bold text-gray-900 mb-4">{event.title}</h2>

          <div className="space-y-2.5 text-sm text-gray-600">
            <div className="flex items-center gap-2">
              <Calendar className="w-4 h-4 text-gray-400" />
              <span>{event.date}</span>
            </div>
            <div className="flex items-center gap-2">
              <Clock className="w-4 h-4 text-gray-400" />
              <span>{event.time}</span>
            </div>
            {cafe && (
              <div className="flex items-center gap-2">
                <MapPin className="w-4 h-4 text-gray-400" />
                <span>{cafe.name} - {cafe.address}</span>
              </div>
            )}
            <div className="flex items-center gap-2">
              <Users className="w-4 h-4 text-gray-400" />
              <span>
                남 {event.currentMaleCount}/{event.maleCapacity} | 여{' '}
                {event.currentFemaleCount}/{event.femaleCapacity}
              </span>
            </div>
            {(event.minAge || event.maxAge) && (
              <div className="flex items-center gap-2">
                <Tag className="w-4 h-4 text-gray-400" />
                <span>
                  {event.minAge && event.maxAge
                    ? `${event.minAge}-${event.maxAge}세`
                    : event.minAge
                    ? `${event.minAge}세 이상`
                    : `${event.maxAge}세 이하`}
                </span>
              </div>
            )}
          </div>

          {event.description && (
            <p className="mt-4 text-sm text-gray-600 leading-relaxed whitespace-pre-wrap">
              {event.description}
            </p>
          )}
        </div>

        {/* Map Section */}
        {cafe && (
          <div className="bg-white rounded-2xl p-4 shadow-sm">
            <h3 className="font-bold text-gray-900 mb-3">위치</h3>
            <KakaoMap
              latitude={cafe.latitude}
              longitude={cafe.longitude}
              name={cafe.name}
            />
            {cafe.latitude && cafe.longitude && (
              <Button
                onClick={handleRoute}
                variant="outline"
                className="w-full mt-3 h-11 rounded-xl gap-2"
              >
                <Navigation className="w-4 h-4" />
                길찾기
              </Button>
            )}
          </div>
        )}

        {/* Action Area */}
        <div className="space-y-3">
          {myApplication ? (
            <div className="bg-white rounded-2xl p-4 shadow-sm">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-500">신청 상태</p>
                  <p
                    className={`text-base font-bold ${
                      myApplication.status === 'APPROVED'
                        ? 'text-green-600'
                        : myApplication.status === 'REJECTED'
                        ? 'text-red-500'
                        : myApplication.status === 'CANCELLED'
                        ? 'text-gray-400'
                        : 'text-gray-700'
                    }`}
                  >
                    {myApplication.status === 'PENDING' && '승인 대기중'}
                    {myApplication.status === 'APPROVED' && '승인됨'}
                    {myApplication.status === 'REJECTED' && '거절됨'}
                    {myApplication.status === 'CANCELLED' && '취소됨'}
                    {myApplication.status === 'COMPLETED' && '완료'}
                  </p>
                </div>
                {myApplication.status === 'PENDING' && (
                  <Button
                    onClick={() => cancelMutation.mutate()}
                    disabled={cancelMutation.isPending}
                    variant="outline"
                    className="rounded-xl text-red-500 border-red-200 hover:bg-red-50"
                  >
                    {cancelMutation.isPending ? (
                      <Loader2 className="w-4 h-4 animate-spin" />
                    ) : (
                      '신청 취소'
                    )}
                  </Button>
                )}
              </div>

              {myApplication.status === 'APPROVED' && event.status === 'CLOSED' && (
                <Button
                  onClick={() => navigate(`/cafes/${slug}/events/${eventId}/choose`)}
                  className="w-full mt-3 h-12 rounded-xl bg-[#FF6B6B] hover:bg-[#FF5252] text-white font-semibold"
                >
                  상대방 선택하기
                </Button>
              )}

              {myApplication.status === 'COMPLETED' && (
                <Button
                  onClick={() => navigate(`/cafes/${slug}/events/${eventId}/result`)}
                  className="w-full mt-3 h-12 rounded-xl bg-[#FF6B6B] hover:bg-[#FF5252] text-white font-semibold"
                >
                  매칭 결과 확인
                </Button>
              )}

              {myApplication.rejectReason && (
                <p className="mt-2 text-xs text-red-400">
                  사유: {myApplication.rejectReason}
                </p>
              )}
            </div>
          ) : (
            event.status === 'OPEN' && (
              <Button
                onClick={() => applyMutation.mutate()}
                disabled={applyMutation.isPending}
                className="w-full h-12 rounded-xl bg-[#FF6B6B] hover:bg-[#FF5252] text-white font-semibold text-base"
              >
                {applyMutation.isPending ? (
                  <Loader2 className="w-5 h-5 animate-spin" />
                ) : (
                  '이벤트 신청하기'
                )}
              </Button>
            )
          )}

          {applyMutation.isError && (
            <p className="text-center text-sm text-red-500">
              신청에 실패했습니다. 다시 시도해주세요.
            </p>
          )}
        </div>
      </div>
    </div>
  )
}
