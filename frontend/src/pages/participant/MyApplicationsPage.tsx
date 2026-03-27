import { useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getMyApplications } from '@/api/participant'
import { cancelApplication } from '@/api/event'
import EmptyState from '@/components/common/EmptyState'
import { Button } from '@/components/ui/button'
import { ArrowLeft, Loader2, Calendar, ClipboardList } from 'lucide-react'
import type { ApplicationStatus } from '@/types'

const STATUS_CONFIG: Record<ApplicationStatus, { label: string; color: string; bgColor: string }> = {
  PENDING: { label: '대기중', color: 'text-gray-600', bgColor: 'bg-gray-100' },
  APPROVED: { label: '승인됨', color: 'text-green-600', bgColor: 'bg-green-50' },
  REJECTED: { label: '거절됨', color: 'text-red-500', bgColor: 'bg-red-50' },
  CANCELLED: { label: '취소됨', color: 'text-gray-400', bgColor: 'bg-gray-50' },
  COMPLETED: { label: '완료', color: 'text-blue-600', bgColor: 'bg-blue-50' },
}

export default function MyApplicationsPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const { data: applications, isLoading } = useQuery({
    queryKey: ['myApplications'],
    queryFn: getMyApplications,
  })

  const cancelMutation = useMutation({
    mutationFn: cancelApplication,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['myApplications'] })
    },
  })

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <Loader2 className="w-8 h-8 text-[#FF6B6B] animate-spin" />
      </div>
    )
  }

  return (
    <div>
      {/* Header */}
      <div className="sticky top-0 z-10 bg-[#FFF5F5] px-4 py-3 flex items-center gap-3">
        <button
          onClick={() => navigate(-1)}
          className="w-10 h-10 flex items-center justify-center rounded-xl hover:bg-white/60 transition-colors"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>
        <h1 className="text-lg font-bold">신청 내역</h1>
      </div>

      <div className="px-4 pb-6">
        {applications && applications.length > 0 ? (
          <div className="space-y-3">
            {applications.map((app) => {
              const config = STATUS_CONFIG[app.status]
              return (
                <div
                  key={app.id}
                  className="bg-white rounded-2xl p-4 shadow-sm"
                >
                  <div className="flex items-start justify-between mb-2">
                    <div className="min-w-0 flex-1">
                      <h3 className="text-base font-bold text-gray-900 truncate">
                        {app.eventTitle || `이벤트 #${app.eventId}`}
                      </h3>
                      {app.cafeName && (
                        <p className="text-sm text-gray-500 truncate">
                          {app.cafeName}
                        </p>
                      )}
                    </div>
                    <span
                      className={`shrink-0 ml-2 px-2.5 py-1 rounded-full text-xs font-semibold ${config.bgColor} ${config.color}`}
                    >
                      {config.label}
                    </span>
                  </div>

                  {app.eventDate && (
                    <div className="flex items-center gap-1.5 text-xs text-gray-400 mb-3">
                      <Calendar className="w-3 h-3" />
                      {app.eventDate}
                    </div>
                  )}

                  {app.rejectReason && (
                    <p className="text-xs text-red-400 mb-3">
                      사유: {app.rejectReason}
                    </p>
                  )}

                  {app.status === 'PENDING' && (
                    <Button
                      onClick={() => cancelMutation.mutate(app.eventId)}
                      disabled={cancelMutation.isPending}
                      variant="outline"
                      className="w-full h-9 rounded-xl text-sm text-red-500 border-red-200 hover:bg-red-50"
                    >
                      {cancelMutation.isPending ? (
                        <Loader2 className="w-4 h-4 animate-spin" />
                      ) : (
                        '신청 취소'
                      )}
                    </Button>
                  )}
                </div>
              )
            })}
          </div>
        ) : (
          <EmptyState
            icon={ClipboardList}
            message="신청 내역이 없습니다"
            description="이벤트에 신청하면 여기에 표시됩니다."
          />
        )}
      </div>
    </div>
  )
}
