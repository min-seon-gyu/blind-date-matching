import { useNavigate } from 'react-router-dom'
import { Calendar, Clock, Users } from 'lucide-react'
import type { Event } from '@/types'

interface EventCardProps {
  event: Event
  cafeSlug: string
}

function StatusBadge({ status }: { status: Event['status'] }) {
  switch (status) {
    case 'OPEN':
      return (
        <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold bg-red-50 text-[#FF6B6B]">
          🔥 모집 중
        </span>
      )
    case 'CLOSED':
      return (
        <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold bg-gray-100 text-gray-500">
          마감
        </span>
      )
    case 'COMPLETED':
      return (
        <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold bg-green-50 text-green-600">
          완료
        </span>
      )
  }
}

function CapacityBar({
  label,
  current,
  capacity,
  color,
}: {
  label: string
  current: number
  capacity: number
  color: string
}) {
  const percentage = capacity > 0 ? Math.min((current / capacity) * 100, 100) : 0

  return (
    <div className="flex items-center gap-2">
      <span className="text-xs text-gray-500 w-8">{label}</span>
      <div className="flex-1 h-2 bg-gray-100 rounded-full overflow-hidden">
        <div
          className="h-full rounded-full transition-all"
          style={{ width: `${percentage}%`, backgroundColor: color }}
        />
      </div>
      <span className="text-xs text-gray-500 w-10 text-right">
        {current}/{capacity}
      </span>
    </div>
  )
}

export default function EventCard({ event, cafeSlug }: EventCardProps) {
  const navigate = useNavigate()

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('ko-KR').format(price)
  }

  return (
    <button
      onClick={() => navigate(`/cafes/${cafeSlug}/events/${event.id}`)}
      className="w-full bg-white rounded-2xl p-4 shadow-sm border border-gray-50 text-left hover:shadow-md transition-shadow"
    >
      {/* Top Row: Status + Price */}
      <div className="flex items-center justify-between mb-3">
        <StatusBadge status={event.status} />
        <span className="text-sm font-bold text-[#FF6B6B]">
          {formatPrice(event.price)}원
        </span>
      </div>

      {/* Title */}
      <h3 className="text-base font-bold text-gray-900 mb-2">{event.title}</h3>

      {/* Date / Time */}
      <div className="flex items-center gap-4 text-sm text-gray-500 mb-3">
        <span className="flex items-center gap-1">
          <Calendar className="w-3.5 h-3.5" />
          {event.date}
        </span>
        <span className="flex items-center gap-1">
          <Clock className="w-3.5 h-3.5" />
          {event.time}
        </span>
      </div>

      {/* Capacity */}
      <div className="space-y-1.5 mb-2">
        <CapacityBar
          label="남"
          current={event.currentMaleCount}
          capacity={event.maleCapacity}
          color="#60A5FA"
        />
        <CapacityBar
          label="여"
          current={event.currentFemaleCount}
          capacity={event.femaleCapacity}
          color="#F472B6"
        />
      </div>

      {/* Tags */}
      <div className="flex items-center gap-2 mt-3">
        <span className="flex items-center gap-1 text-xs text-gray-400">
          <Users className="w-3 h-3" />
          {event.currentMaleCount + event.currentFemaleCount}/
          {event.maleCapacity + event.femaleCapacity}
        </span>
        {(event.minAge || event.maxAge) && (
          <span className="px-2 py-0.5 rounded-full bg-orange-50 text-orange-500 text-xs">
            {event.minAge && event.maxAge
              ? `${event.minAge}-${event.maxAge}세`
              : event.minAge
              ? `${event.minAge}세 이상`
              : `${event.maxAge}세 이하`}
          </span>
        )}
      </div>
    </button>
  )
}
