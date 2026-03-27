import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { organizerApi } from '@/api/organizer'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
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
import { Loader2, Handshake } from 'lucide-react'
import type { Partnership } from '@/types'

function partnershipStatusBadge(status: string) {
  switch (status) {
    case 'PENDING':
      return <Badge className="bg-yellow-100 text-yellow-700">대기</Badge>
    case 'ACTIVE':
      return <Badge className="bg-green-100 text-green-700">활성</Badge>
    case 'TERMINATED':
      return <Badge className="bg-gray-100 text-gray-600">해지</Badge>
    default:
      return <Badge variant="secondary">{status}</Badge>
  }
}

export default function OrganizerPartnershipsPage() {
  const navigate = useNavigate()

  const { data: partnerships, isLoading } = useQuery<Partnership[]>({
    queryKey: ['organizer', 'partnerships'],
    queryFn: () => organizerApi.getPartnerships().then((r) => r.data),
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
        <h1 className="text-2xl font-bold">제휴 카페</h1>
        <Button
          className="bg-[#FF6B6B] hover:bg-[#FF5252] text-white"
          onClick={() => navigate('/marketplace')}
        >
          마켓플레이스 가기
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>제휴 목록</CardTitle>
        </CardHeader>
        <CardContent>
          {!partnerships || partnerships.length === 0 ? (
            <EmptyState
              icon={Handshake}
              message="제휴 카페가 없습니다"
              description="마켓플레이스에서 카페를 찾아보세요."
              actionLabel="마켓플레이스 가기"
              onAction={() => navigate('/marketplace')}
            />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>카페명</TableHead>
                  <TableHead>상태</TableHead>
                  <TableHead>요청일</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {partnerships.map((p) => (
                  <TableRow key={p.id}>
                    <TableCell className="font-medium">{p.cafeName}</TableCell>
                    <TableCell>{partnershipStatusBadge(p.status)}</TableCell>
                    <TableCell>
                      {new Date(p.createdAt).toLocaleDateString('ko-KR')}
                    </TableCell>
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
