import { useQuery } from '@tanstack/react-query'
import { adminApi } from '@/api/admin'
import { Card, CardContent } from '@/components/ui/card'
import { Loader2, Coffee, Users, CalendarCheck, Receipt } from 'lucide-react'
import type { DashboardStats } from '@/types'

export default function AdminDashboardPage() {
  const { data: dashboard, isLoading } = useQuery<{ stats: DashboardStats }>({
    queryKey: ['admin', 'dashboard'],
    queryFn: () => adminApi.getDashboard().then((r) => r.data),
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
      <h1 className="text-2xl font-bold">관리자 대시보드</h1>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card>
          <CardContent className="flex items-center gap-4">
            <div className="w-10 h-10 rounded-lg bg-amber-50 flex items-center justify-center">
              <Coffee className="w-5 h-5 text-amber-600" />
            </div>
            <div>
              <p className="text-sm text-gray-500">카페 수</p>
              <p className="text-2xl font-bold">{stats?.totalCafes ?? 0}</p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="flex items-center gap-4">
            <div className="w-10 h-10 rounded-lg bg-blue-50 flex items-center justify-center">
              <Users className="w-5 h-5 text-blue-500" />
            </div>
            <div>
              <p className="text-sm text-gray-500">주관자 수</p>
              <p className="text-2xl font-bold">{stats?.totalOrganizers ?? 0}</p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="flex items-center gap-4">
            <div className="w-10 h-10 rounded-lg bg-[#FFF5F5] flex items-center justify-center">
              <CalendarCheck className="w-5 h-5 text-[#FF6B6B]" />
            </div>
            <div>
              <p className="text-sm text-gray-500">총 이벤트</p>
              <p className="text-2xl font-bold">{stats?.totalEvents ?? 0}</p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="flex items-center gap-4">
            <div className="w-10 h-10 rounded-lg bg-orange-50 flex items-center justify-center">
              <Receipt className="w-5 h-5 text-orange-500" />
            </div>
            <div>
              <p className="text-sm text-gray-500">총 수수료</p>
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
