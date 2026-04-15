import { useQuery } from '@tanstack/react-query'
import { cafeOwnerApi } from '@/api/cafeOwner'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import EmptyState from '@/components/common/EmptyState'
import { Loader2, CalendarX } from 'lucide-react'
import type { Event } from '@/types'

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

export default function CafeOwnerEventsPage() {
  const { data: events, isLoading } = useQuery<Event[]>({
    queryKey: ['cafeOwner', 'events'],
    queryFn: () => cafeOwnerApi.getEvents().then((r) => r.data),
  })

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <Loader2 className="w-8 h-8 text-[#FF6B6B] animate-spin" />
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">이벤트 현황</h1>

      <Card>
        <CardHeader>
          <CardTitle>내 카페 이벤트</CardTitle>
        </CardHeader>
        <CardContent>
          {!events || events.length === 0 ? (
            <EmptyState
              icon={CalendarX}
              message="이벤트가 없습니다"
              description="주관자가 이벤트를 생성하면 여기에 표시됩니다."
            />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>제목</TableHead>
                  <TableHead>날짜</TableHead>
                  <TableHead>시간</TableHead>
                  <TableHead>정원 (남/여)</TableHead>
                  <TableHead>상태</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {events.map((event) => (
                  <TableRow key={event.id}>
                    <TableCell className="font-medium">{event.title}</TableCell>
                    <TableCell>{event.date}</TableCell>
                    <TableCell>{event.time}</TableCell>
                    <TableCell>
                      {event.currentMaleCount}/{event.maleCapacity} ·{' '}
                      {event.currentFemaleCount}/{event.femaleCapacity}
                    </TableCell>
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
