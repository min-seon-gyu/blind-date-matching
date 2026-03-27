import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { cafeOwnerApi } from '@/api/cafeOwner'
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
import { Loader2, Handshake, Check, X, ChevronDown, ChevronUp } from 'lucide-react'
import type { Partnership } from '@/types'

export default function CafeOwnerPartnershipsPage() {
  const queryClient = useQueryClient()
  const [showTerminated, setShowTerminated] = useState(false)

  const { data: partnerships, isLoading } = useQuery<Partnership[]>({
    queryKey: ['cafeOwner', 'partnerships'],
    queryFn: () => cafeOwnerApi.getPartnerships().then((r) => r.data),
  })

  const acceptMutation = useMutation({
    mutationFn: (id: number) => cafeOwnerApi.acceptPartnership(id),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: ['cafeOwner', 'partnerships'] }),
  })

  const rejectMutation = useMutation({
    mutationFn: (id: number) => cafeOwnerApi.rejectPartnership(id),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: ['cafeOwner', 'partnerships'] }),
  })

  const terminateMutation = useMutation({
    mutationFn: (id: number) => cafeOwnerApi.terminatePartnership(id),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: ['cafeOwner', 'partnerships'] }),
  })

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <Loader2 className="w-8 h-8 text-[#FF6B6B] animate-spin" />
      </div>
    )
  }

  const pending = partnerships?.filter((p) => p.status === 'PENDING') ?? []
  const active = partnerships?.filter((p) => p.status === 'ACTIVE') ?? []
  const terminated = partnerships?.filter((p) => p.status === 'TERMINATED') ?? []

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">제휴 관리</h1>

      {/* Pending Requests */}
      <Card>
        <CardHeader>
          <CardTitle>받은 요청</CardTitle>
        </CardHeader>
        <CardContent>
          {pending.length === 0 ? (
            <p className="text-sm text-gray-400 py-4 text-center">
              대기 중인 요청이 없습니다.
            </p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>주관자</TableHead>
                  <TableHead>요청일</TableHead>
                  <TableHead>메시지</TableHead>
                  <TableHead>관리</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {pending.map((p) => (
                  <TableRow key={p.id}>
                    <TableCell className="font-medium">
                      {p.organizerName}
                    </TableCell>
                    <TableCell>
                      {new Date(p.createdAt).toLocaleDateString('ko-KR')}
                    </TableCell>
                    <TableCell className="max-w-[200px] truncate">
                      {p.message || '-'}
                    </TableCell>
                    <TableCell>
                      <div className="flex gap-2">
                        <Button
                          size="xs"
                          className="bg-green-500 hover:bg-green-600 text-white"
                          onClick={() => acceptMutation.mutate(p.id)}
                          disabled={acceptMutation.isPending}
                        >
                          <Check className="w-3 h-3" />
                          수락
                        </Button>
                        <Button
                          size="xs"
                          variant="destructive"
                          onClick={() => rejectMutation.mutate(p.id)}
                          disabled={rejectMutation.isPending}
                        >
                          <X className="w-3 h-3" />
                          거절
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      {/* Active Partnerships */}
      <Card>
        <CardHeader>
          <CardTitle>활성 제휴</CardTitle>
        </CardHeader>
        <CardContent>
          {active.length === 0 ? (
            <EmptyState
              icon={Handshake}
              message="활성 제휴가 없습니다"
              description="주관자의 제휴 요청을 수락하면 여기에 표시됩니다."
            />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>주관자</TableHead>
                  <TableHead>상태</TableHead>
                  <TableHead>요청일</TableHead>
                  <TableHead>관리</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {active.map((p) => (
                  <TableRow key={p.id}>
                    <TableCell className="font-medium">
                      {p.organizerName}
                    </TableCell>
                    <TableCell>
                      <Badge className="bg-green-100 text-green-700">활성</Badge>
                    </TableCell>
                    <TableCell>
                      {new Date(p.createdAt).toLocaleDateString('ko-KR')}
                    </TableCell>
                    <TableCell>
                      <Button
                        size="xs"
                        variant="destructive"
                        onClick={() => {
                          if (confirm('정말 해지하시겠습니까?'))
                            terminateMutation.mutate(p.id)
                        }}
                        disabled={terminateMutation.isPending}
                      >
                        해지
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      {/* Terminated (collapsed) */}
      {terminated.length > 0 && (
        <Card>
          <CardHeader>
            <button
              onClick={() => setShowTerminated(!showTerminated)}
              className="flex items-center gap-2 w-full text-left"
            >
              <CardTitle>해지된 제휴 ({terminated.length})</CardTitle>
              {showTerminated ? (
                <ChevronUp className="w-4 h-4 text-gray-400" />
              ) : (
                <ChevronDown className="w-4 h-4 text-gray-400" />
              )}
            </button>
          </CardHeader>
          {showTerminated && (
            <CardContent>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>주관자</TableHead>
                    <TableHead>상태</TableHead>
                    <TableHead>요청일</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {terminated.map((p) => (
                    <TableRow key={p.id}>
                      <TableCell className="font-medium">
                        {p.organizerName}
                      </TableCell>
                      <TableCell>
                        <Badge className="bg-gray-100 text-gray-600">해지</Badge>
                      </TableCell>
                      <TableCell>
                        {new Date(p.createdAt).toLocaleDateString('ko-KR')}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </CardContent>
          )}
        </Card>
      )}
    </div>
  )
}
