import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { getCafe, getCafeEvents } from '@/api/event'
import EventCard from '@/components/participant/EventCard'
import EmptyState from '@/components/common/EmptyState'
import { Loader2, MapPin, CalendarX } from 'lucide-react'

export default function CafeMainPage() {
  const { slug } = useParams<{ slug: string }>()

  const { data: cafe, isLoading: cafeLoading, isError: cafeError } = useQuery({
    queryKey: ['cafe', slug],
    queryFn: () => getCafe(slug!),
    enabled: !!slug,
  })

  const { data: events, isLoading: eventsLoading } = useQuery({
    queryKey: ['cafeEvents', slug],
    queryFn: () => getCafeEvents(slug!),
    enabled: !!slug,
  })

  if (cafeLoading || eventsLoading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <Loader2 className="w-8 h-8 text-[#FF6B6B] animate-spin" />
      </div>
    )
  }

  if (cafeError || !cafe) {
    return (
      <EmptyState
        emoji="😢"
        message="카페를 찾을 수 없습니다"
        description="잘못된 주소이거나 삭제된 카페입니다."
      />
    )
  }

  return (
    <div>
      {/* Gradient Header */}
      <div className="relative overflow-hidden">
        {cafe.coverImageUrl ? (
          <div className="relative h-48">
            <img
              src={cafe.coverImageUrl}
              alt={cafe.name}
              className="w-full h-full object-cover"
            />
            <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent" />
            <div className="absolute bottom-0 left-0 right-0 p-4 text-white">
              <h1 className="text-2xl font-bold">{cafe.name}</h1>
              {cafe.description && (
                <p className="mt-1 text-sm text-white/80">{cafe.description}</p>
              )}
              <div className="flex items-center gap-1 mt-2 text-xs text-white/70">
                <MapPin className="w-3 h-3" />
                {cafe.address}
              </div>
            </div>
          </div>
        ) : (
          <div className="bg-gradient-to-br from-[#FF6B6B] to-[#FFE66D] px-4 py-8">
            {cafe.logoUrl && (
              <img
                src={cafe.logoUrl}
                alt={cafe.name}
                className="w-16 h-16 rounded-xl object-cover mb-3"
              />
            )}
            <h1 className="text-2xl font-bold text-white">{cafe.name}</h1>
            {cafe.description && (
              <p className="mt-1 text-sm text-white/80">{cafe.description}</p>
            )}
            <div className="flex items-center gap-1 mt-2 text-xs text-white/70">
              <MapPin className="w-3 h-3" />
              {cafe.address}
            </div>
          </div>
        )}
      </div>

      {/* Events List */}
      <div className="px-4 py-5">
        <h2 className="text-lg font-bold text-gray-900 mb-4">진행 중인 이벤트</h2>

        {events && events.length > 0 ? (
          <div className="space-y-3">
            {events.map((event) => (
              <EventCard key={event.id} event={event} cafeSlug={slug!} />
            ))}
          </div>
        ) : (
          <EmptyState
            icon={CalendarX}
            message="예정된 이벤트가 없습니다"
            description="곧 새로운 이벤트가 열릴 예정이에요!"
          />
        )}
      </div>
    </div>
  )
}
