import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { adminApi } from '@/api/admin'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs'
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

function CommissionTable({
  commissions,
  onInvoice,
  onMarkPaid,
  invoicePending,
  paidPending,
}: {
  commissions: Commission[]
  onInvoice: (id: number) => void
  onMarkPaid: (id: number) => void
  invoicePending: boolean
  paidPending: boolean
}) {
  if (commissions.length === 0) {
    return (
      <EmptyState
        icon={Receipt}
        message="수수료 내역이 없습니다"
      />
    )
  }

  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>이벤트명</TableHead>
          <TableHead>카페명</TableHead>
          <TableHead>참가자 수</TableHead>
          <TableHead>금액</TableHead>
          <TableHead>상태</TableHead>
          <TableHead>관리</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {commissions.map((c) => (
          <TableRow key={c.id}>
            <TableCell className="font-medium">{c.eventTitle}</TableCell>
            <TableCell>{c.cafeName}</TableCell>
            <TableCell>{c.participantCount}명</TableCell>
            <TableCell>{c.totalAmount.toLocaleString()}원</TableCell>
            <TableCell>{commissionStatusBadge(c.status)}</TableCell>
            <TableCell>
              <div className="flex gap-2">
                {c.status === 'PENDING' && (
                  <Button
                    size="xs"
                    className="bg-blue-500 hover:bg-blue-600 text-white"
                    onClick={() => onInvoice(c.id)}
                    disabled={invoicePending}
                  >
                    청구
                  </Button>
                )}
                {c.status === 'INVOICED' && (
                  <Button
                    size="xs"
                    className="bg-green-500 hover:bg-green-600 text-white"
                    onClick={() => onMarkPaid(c.id)}
                    disabled={paidPending}
                  >
                    입금확인
                  </Button>
                )}
              </div>
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  )
}

export default function AdminCommissionsPage() {
  const queryClient = useQueryClient()

  const { data: commissions, isLoading } = useQuery<Commission[]>({
    queryKey: ['admin', 'commissions'],
    queryFn: () => adminApi.getCommissions().then((r) => r.data),
  })

  const invoiceMutation = useMutation({
    mutationFn: (id: number) => adminApi.invoiceCommission(id),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: ['admin', 'commissions'] }),
  })

  const paidMutation = useMutation({
    mutationFn: (id: number) => adminApi.markCommissionPaid(id),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: ['admin', 'commissions'] }),
  })

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <Loader2 className="w-8 h-8 text-[#FF6B6B] animate-spin" />
      </div>
    )
  }

  const organizerCommissions =
    commissions?.filter((c) => c.targetType === 'ORGANIZER') ?? []
  const cafeCommissions =
    commissions?.filter((c) => c.targetType === 'CAFE_OWNER') ?? []

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">수수료 관리</h1>

      <Tabs defaultValue="organizer">
        <TabsList>
          <TabsTrigger value="organizer">주관자 수수료</TabsTrigger>
          <TabsTrigger value="cafe">카페 수수료</TabsTrigger>
        </TabsList>

        <TabsContent value="organizer">
          <Card>
            <CardHeader>
              <CardTitle>주관자 수수료</CardTitle>
            </CardHeader>
            <CardContent>
              <CommissionTable
                commissions={organizerCommissions}
                onInvoice={(id) => invoiceMutation.mutate(id)}
                onMarkPaid={(id) => paidMutation.mutate(id)}
                invoicePending={invoiceMutation.isPending}
                paidPending={paidMutation.isPending}
              />
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="cafe">
          <Card>
            <CardHeader>
              <CardTitle>카페 수수료</CardTitle>
            </CardHeader>
            <CardContent>
              <CommissionTable
                commissions={cafeCommissions}
                onInvoice={(id) => invoiceMutation.mutate(id)}
                onMarkPaid={(id) => paidMutation.mutate(id)}
                invoicePending={invoiceMutation.isPending}
                paidPending={paidMutation.isPending}
              />
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}
