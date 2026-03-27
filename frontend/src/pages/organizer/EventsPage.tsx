import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { organizerApi } from '@/api/organizer'
import { Button } from '@/components/ui/button'
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
import { Loader2, Plus, CalendarX } from 'lucide-react'
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

export default function OrganizerEventsPage() {
  const navigate = useNavigate()

  const { data: events, isLoading } = useQuery<Event[]>({
    queryKey: ['organizer', 'events'],
    queryFn: () => organizerApi.getEvents().then((r) => r.data),
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
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">이벤트 관리</h1>
        <Button
          className="bg-[#FF6B6B] hover:bg-[#FF5252] text-white"
          onClick={() => navigate('/organizer/events/new')}
        >
          <Plus className="w-4 h-4" />
          이벤트 생성
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>전체 이벤트</CardTitle>
        </CardHeader>
        <CardContent>
          {!events || events.length === 0 ? (
            <EmptyState
              icon={CalendarX}
              message="등록된 이벤트가 없습니다"
              description="새 이벤트를 생성해보세요."
              actionLabel="이벤트 생성"
              onAction={() => navigate('/organizer/events/new')}
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
                  <TableRow
                    key={event.id}
                    className="cursor-pointer"
                    onClick={() => navigate(`/organizer/events/${event.id}`)}
                  >
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
