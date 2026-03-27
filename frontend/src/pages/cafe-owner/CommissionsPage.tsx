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
import { Loader2, Receipt } from 'lucide-react'
import type { Commission } from '@/types'

function commissionStatusBadge(status: string) {
  switch (status) {
    case 'PENDING':
      return <Badge className="bg-yellow-100 text-yellow-700">대기</Badge>
    case 'INVOICED':
      return <Badge className="bg-blue-100 text-blue-700">청구됨</Badge>
    case 'PAID':
      return <Badge className="bg-green-100 text-green-700">입금 완료</Badge>
    default:
      return <Badge variant="secondary">{status}</Badge>
  }
}

export default function CafeOwnerCommissionsPage() {
  const { data: commissions, isLoading } = useQuery<Commission[]>({
    queryKey: ['cafeOwner', 'commissions'],
    queryFn: () => cafeOwnerApi.getCommissions().then((r) => r.data),
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
      <h1 className="text-2xl font-bold">수수료</h1>

      <Card>
        <CardHeader>
          <CardTitle>수수료 내역</CardTitle>
        </CardHeader>
        <CardContent>
          {!commissions || commissions.length === 0 ? (
            <EmptyState
              icon={Receipt}
              message="수수료 내역이 없습니다"
              description="이벤트가 완료되면 수수료가 생성됩니다."
            />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>이벤트명</TableHead>
                  <TableHead>참가자 수</TableHead>
                  <TableHead>금액</TableHead>
                  <TableHead>상태</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {commissions.map((c) => (
                  <TableRow key={c.id}>
                    <TableCell className="font-medium">{c.eventTitle}</TableCell>
                    <TableCell>{c.participantCount}명</TableCell>
                    <TableCell>{c.totalAmount.toLocaleString()}원</TableCell>
                    <TableCell>{commissionStatusBadge(c.status)}</TableCell>
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
