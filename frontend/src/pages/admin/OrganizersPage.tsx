import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { adminApi } from '@/api/admin'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  DialogTrigger,
  DialogClose,
} from '@/components/ui/dialog'
import EmptyState from '@/components/common/EmptyState'
import { Loader2, Plus, Users } from 'lucide-react'
import type { Organizer } from '@/types'

export default function AdminOrganizersPage() {
  const queryClient = useQueryClient()

  const { data: organizers, isLoading } = useQuery<Organizer[]>({
    queryKey: ['admin', 'organizers'],
    queryFn: () => adminApi.getOrganizers().then((r) => r.data),
  })

  const [form, setForm] = useState({
    name: '',
    email: '',
    password: '',
    phoneNumber: '',
  })
  const [dialogOpen, setDialogOpen] = useState(false)

  const mutation = useMutation({
    mutationFn: (data: Record<string, string>) => adminApi.createOrganizer(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'organizers'] })
      setForm({ name: '', email: '', password: '', phoneNumber: '' })
      setDialogOpen(false)
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
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">주관자 관리</h1>
        <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
          <DialogTrigger
            render={
              <Button className="bg-[#FF6B6B] hover:bg-[#FF5252] text-white">
                <Plus className="w-4 h-4" />
                주관자 등록
              </Button>
            }
          />
          <DialogContent>
            <DialogHeader>
              <DialogTitle>주관자 등록</DialogTitle>
            </DialogHeader>
            <form
              onSubmit={(e) => {
                e.preventDefault()
                mutation.mutate(form)
              }}
              className="space-y-4"
            >
              <div className="space-y-1.5">
                <Label htmlFor="orgName">이름</Label>
                <Input
                  id="orgName"
                  value={form.name}
                  onChange={(e) =>
                    setForm((p) => ({ ...p, name: e.target.value }))
                  }
                  required
                />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="orgEmail">이메일</Label>
                <Input
                  id="orgEmail"
                  type="email"
                  value={form.email}
                  onChange={(e) =>
                    setForm((p) => ({ ...p, email: e.target.value }))
                  }
                  required
                />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="orgPassword">비밀번호</Label>
                <Input
                  id="orgPassword"
                  type="password"
                  value={form.password}
                  onChange={(e) =>
                    setForm((p) => ({ ...p, password: e.target.value }))
                  }
                  required
                />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="orgPhone">전화번호</Label>
                <Input
                  id="orgPhone"
                  value={form.phoneNumber}
                  onChange={(e) =>
                    setForm((p) => ({ ...p, phoneNumber: e.target.value }))
                  }
                  placeholder="010-0000-0000"
                  required
                />
              </div>
              {mutation.isError && (
                <p className="text-sm text-red-500">등록에 실패했습니다.</p>
              )}
              <DialogFooter>
                <DialogClose render={<Button variant="outline" />}>
                  취소
                </DialogClose>
                <Button
                  type="submit"
                  disabled={mutation.isPending}
                  className="bg-[#FF6B6B] hover:bg-[#FF5252] text-white"
                >
                  {mutation.isPending ? (
                    <Loader2 className="w-4 h-4 animate-spin" />
                  ) : (
                    '등록'
                  )}
                </Button>
              </DialogFooter>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>전체 주관자</CardTitle>
        </CardHeader>
        <CardContent>
          {!organizers || organizers.length === 0 ? (
            <EmptyState
              icon={Users}
              message="등록된 주관자가 없습니다"
              description="주관자를 등록해보세요."
            />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>ID</TableHead>
                  <TableHead>이름</TableHead>
                  <TableHead>이메일</TableHead>
                  <TableHead>전화번호</TableHead>
                  <TableHead>수수료율</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {organizers.map((org) => (
                  <TableRow key={org.id}>
                    <TableCell>{org.id}</TableCell>
                    <TableCell className="font-medium">{org.name}</TableCell>
                    <TableCell>{org.email}</TableCell>
                    <TableCell>{org.phoneNumber}</TableCell>
                    <TableCell>{(org.commissionRate * 100).toFixed(0)}%</TableCell>
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
