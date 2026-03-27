import { useQuery } from '@tanstack/react-query'
import { cafeOwnerApi } from '@/api/cafeOwner'
import { Card, CardContent } from '@/components/ui/card'
import { Loader2, CalendarCheck, Handshake, Receipt } from 'lucide-react'
import type { DashboardStats } from '@/types'

export default function CafeOwnerDashboardPage() {
  const { data: dashboard, isLoading } = useQuery<{ stats: DashboardStats }>({
    queryKey: ['cafeOwner', 'dashboard'],
    queryFn: () => cafeOwnerApi.getDashboard().then((r) => r.data),
  })

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <Loader2 className="w-8 h-8 text-[#FF6B6B] animate-spin" />
      </div>
    )
  }

  const stats = dashboard?.stats

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">대시보드</h1>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <Card>
          <CardContent className="flex items-center gap-4">
            <div className="w-10 h-10 rounded-lg bg-[#FFF5F5] flex items-center justify-center">
              <CalendarCheck className="w-5 h-5 text-[#FF6B6B]" />
            </div>
            <div>
              <p className="text-sm text-gray-500">예정 이벤트</p>
              <p className="text-2xl font-bold">{stats?.upcomingEvents ?? 0}</p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="flex items-center gap-4">
            <div className="w-10 h-10 rounded-lg bg-blue-50 flex items-center justify-center">
              <Handshake className="w-5 h-5 text-blue-500" />
            </div>
            <div>
              <p className="text-sm text-gray-500">제휴 수</p>
              <p className="text-2xl font-bold">{stats?.activePartnerships ?? 0}</p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="flex items-center gap-4">
            <div className="w-10 h-10 rounded-lg bg-orange-50 flex items-center justify-center">
              <Receipt className="w-5 h-5 text-orange-500" />
            </div>
            <div>
              <p className="text-sm text-gray-500">수수료 합계</p>
              <p className="text-2xl font-bold">
                {Number(stats?.totalCommission ?? 0).toLocaleString()}원
              </p>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
