import { useQuery } from '@tanstack/react-query'
import { organizerApi } from '@/api/organizer'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { Loader2, CalendarCheck, Users, Receipt } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import type { Event, DashboardStats } from '@/types'

function statusBadge(status: string) {
  switch (status) {
    case 'OPEN':
      return <Badge className="bg-green-100 text-green-700">모집 중</Badge>
    case 'CLOSED':
      return <Badge className="bg-yellow-100 text-yellow-700">마감</Badge>
    case 'COMPLETED':
      return <Badge className="bg-gray-100 text-gray-600">완료</Badge>
    default:
      return <Badge variant="secondary">{status}</Badge>
  }
}

export default function OrganizerDashboardPage() {
  const navigate = useNavigate()

  const { data: dashboard, isLoading: dashLoading } = useQuery<{
    stats: DashboardStats
    recentEvents: Event[]
  }>({
    queryKey: ['organizer', 'dashboard'],
    queryFn: () => organizerApi.getDashboard().then((r) => r.data),
  })

  if (dashLoading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <Loader2 className="w-8 h-8 text-[#FF6B6B] animate-spin" />
      </div>
    )
  }

  const stats = dashboard?.stats
  const recentEvents = dashboard?.recentEvents ?? []

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">대시보드</h1>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <Card>
          <CardContent className="flex items-center gap-4">
            <div className="w-10 h-10 rounded-lg bg-[#FFF5F5] flex items-center justify-center">
              <CalendarCheck className="w-5 h-5 text-[#FF6B6B]" />
            </div>
            <div>
              <p className="text-sm text-gray-500">진행 중 이벤트</p>
              <p className="text-2xl font-bold">{stats?.activeEvents ?? 0}</p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="flex items-center gap-4">
            <div className="w-10 h-10 rounded-lg bg-blue-50 flex items-center justify-center">
              <Users className="w-5 h-5 text-blue-500" />
            </div>
            <div>
              <p className="text-sm text-gray-500">총 참가자</p>
              <p className="text-2xl font-bold">{stats?.totalParticipants ?? 0}</p>
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

      {/* Recent Events */}
      <Card>
        <CardHeader>
          <CardTitle>최근 이벤트</CardTitle>
        </CardHeader>
        <CardContent>
          {recentEvents.length === 0 ? (
            <p className="text-sm text-gray-400 py-4 text-center">
              등록된 이벤트가 없습니다.
            </p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>제목</TableHead>
                  <TableHead>날짜</TableHead>
                  <TableHead>상태</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {recentEvents.map((event) => (
                  <TableRow
                    key={event.id}
                    className="cursor-pointer"
                    onClick={() => navigate(`/organizer/events/${event.id}`)}
                  >
                    <TableCell className="font-medium">{event.title}</TableCell>
                    <TableCell>{event.date}</TableCell>
                    <TableCell>{statusBadge(event.status)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
