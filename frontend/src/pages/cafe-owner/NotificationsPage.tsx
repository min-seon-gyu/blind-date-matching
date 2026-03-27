import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { cafeOwnerApi } from '@/api/cafeOwner'
import EmptyState from '@/components/common/EmptyState'
import {
  Loader2,
  Bell,
  CheckCircle,
  XCircle,
  Heart,
  CalendarCheck,
  Clock,
  Receipt,
  Handshake,
  BellOff,
} from 'lucide-react'
import type { Notification, NotificationType } from '@/types'

function getNotificationIcon(type: NotificationType) {
  switch (type) {
    case 'NEW_APPLICATION':
      return <Bell className="w-5 h-5 text-blue-500" />
    case 'APPROVED':
      return <CheckCircle className="w-5 h-5 text-green-500" />
    case 'REJECTED':
      return <XCircle className="w-5 h-5 text-red-500" />
    case 'MATCH_RESULT':
      return <Heart className="w-5 h-5 text-[#FF6B6B]" />
    case 'EVENT_REMINDER':
      return <CalendarCheck className="w-5 h-5 text-orange-500" />
    case 'CHOICE_REMINDER':
      return <Clock className="w-5 h-5 text-purple-500" />
    case 'EVENT_COMPLETED':
      return <CheckCircle className="w-5 h-5 text-blue-500" />
    case 'COMMISSION_INVOICE':
      return <Receipt className="w-5 h-5 text-gray-500" />
    case 'PARTNERSHIP_REQUESTED':
    case 'PARTNERSHIP_ACCEPTED':
    case 'PARTNERSHIP_REJECTED':
      return <Handshake className="w-5 h-5 text-teal-500" />
    default:
      return <Bell className="w-5 h-5 text-gray-400" />
  }
}

function getRelativeTime(dateStr: string): string {
  const diff = Date.now() - new Date(dateStr).getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '방금 전'
  if (minutes < 60) return `${minutes}분 전`
  if (hours < 24) return `${hours}시간 전`
  if (days < 7) return `${days}일 전`
  return new Date(dateStr).toLocaleDateString('ko-KR')
}

export default function CafeOwnerNotificationsPage() {
  const queryClient = useQueryClient()

  const { data: notifications, isLoading } = useQuery<Notification[]>({
    queryKey: ['cafeOwner', 'notifications'],
    queryFn: () => cafeOwnerApi.getNotifications().then((r) => r.data),
  })

  const readMutation = useMutation({
    mutationFn: (id: number) => cafeOwnerApi.markNotificationAsRead(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['cafeOwner', 'notifications'] })
    },
  })

  const handleClick = (id: number, isRead: boolean) => {
    if (!isRead) readMutation.mutate(id)
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <Loader2 className="w-8 h-8 text-[#FF6B6B] animate-spin" />
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">알림</h1>

      {notifications && notifications.length > 0 ? (
        <div className="space-y-2">
          {notifications.map((notif) => (
            <button
              key={notif.id}
              onClick={() => handleClick(notif.id, notif.isRead)}
              className={`w-full text-left rounded-xl p-4 transition-colors ${
                notif.isRead
                  ? 'bg-white ring-1 ring-gray-100'
                  : 'bg-[#FFF0F0] ring-1 ring-[#FF6B6B]/10'
              }`}
            >
              <div className="flex items-start gap-3">
                <div className="shrink-0 mt-0.5">
                  {getNotificationIcon(notif.type)}
                </div>
                <div className="min-w-0 flex-1">
                  <div className="flex items-center justify-between gap-2">
                    <h3
                      className={`text-sm font-semibold truncate ${
                        notif.isRead ? 'text-gray-700' : 'text-gray-900'
                      }`}
                    >
                      {notif.title}
                    </h3>
                    {!notif.isRead && (
                      <div className="w-2 h-2 rounded-full bg-[#FF6B6B] shrink-0" />
                    )}
                  </div>
                  <p className="text-sm text-gray-500 mt-0.5 line-clamp-2">
                    {notif.message}
                  </p>
                  <p className="text-xs text-gray-400 mt-1">
                    {getRelativeTime(notif.createdAt)}
                  </p>
                </div>
              </div>
            </button>
          ))}
        </div>
      ) : (
        <EmptyState
          icon={BellOff}
          message="알림이 없습니다"
          description="새로운 소식이 있으면 알려드릴게요."
        />
      )}
    </div>
  )
}
