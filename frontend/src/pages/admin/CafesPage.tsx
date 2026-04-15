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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import EmptyState from '@/components/common/EmptyState'
import { Loader2, Plus, Coffee } from 'lucide-react'
import type { Cafe } from '@/types'

export default function AdminCafesPage() {
  const queryClient = useQueryClient()

  const { data: cafes, isLoading } = useQuery<Cafe[]>({
    queryKey: ['admin', 'cafes'],
    queryFn: () => adminApi.getCafes().then((r) => r.data),
  })

  // Cafe creation form
  const [cafeForm, setCafeForm] = useState({ name: '', address: '', slug: '' })
  const [cafeDialogOpen, setCafeDialogOpen] = useState(false)

  const createCafeMutation = useMutation({
    mutationFn: (data: Record<string, string>) => adminApi.createCafe(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'cafes'] })
      setCafeForm({ name: '', address: '', slug: '' })
      setCafeDialogOpen(false)
    },
  })

  // Cafe Owner creation form
  const [ownerForm, setOwnerForm] = useState({
    name: '',
    email: '',
    password: '',
    phoneNumber: '',
    cafeId: '',
  })
  const [ownerDialogOpen, setOwnerDialogOpen] = useState(false)

  const createOwnerMutation = useMutation({
    mutationFn: (data: Record<string, unknown>) => adminApi.createCafeOwner(data),
    onSuccess: () => {
      setOwnerForm({ name: '', email: '', password: '', phoneNumber: '', cafeId: '' })
      setOwnerDialogOpen(false)
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
        <h1 className="text-2xl font-bold">카페 관리</h1>
        <div className="flex gap-2">
          {/* Create Cafe Dialog */}
          <Dialog open={cafeDialogOpen} onOpenChange={setCafeDialogOpen}>
            <DialogTrigger
              render={
                <Button className="bg-[#FF6B6B] hover:bg-[#FF5252] text-white">
                  <Plus className="w-4 h-4" />
                  카페 등록
                </Button>
              }
            />
            <DialogContent>
              <DialogHeader>
                <DialogTitle>카페 등록</DialogTitle>
              </DialogHeader>
              <form
                onSubmit={(e) => {
                  e.preventDefault()
                  createCafeMutation.mutate(cafeForm)
                }}
                className="space-y-4"
              >
                <div className="space-y-1.5">
                  <Label htmlFor="cafeName">카페명</Label>
                  <Input
                    id="cafeName"
                    value={cafeForm.name}
                    onChange={(e) =>
                      setCafeForm((p) => ({ ...p, name: e.target.value }))
                    }
                    required
                  />
                </div>
                <div className="space-y-1.5">
                  <Label htmlFor="cafeAddress">주소</Label>
                  <Input
                    id="cafeAddress"
                    value={cafeForm.address}
                    onChange={(e) =>
                      setCafeForm((p) => ({ ...p, address: e.target.value }))
                    }
                    required
                  />
                </div>
                <div className="space-y-1.5">
                  <Label htmlFor="cafeSlug">슬러그</Label>
                  <Input
                    id="cafeSlug"
                    value={cafeForm.slug}
                    onChange={(e) =>
                      setCafeForm((p) => ({ ...p, slug: e.target.value }))
                    }
                    placeholder="my-cafe"
                    required
                  />
                </div>
                {createCafeMutation.isError && (
                  <p className="text-sm text-red-500">카페 등록에 실패했습니다.</p>
                )}
                <DialogFooter>
                  <DialogClose render={<Button variant="outline" />}>
                    취소
                  </DialogClose>
                  <Button
                    type="submit"
                    disabled={createCafeMutation.isPending}
                    className="bg-[#FF6B6B] hover:bg-[#FF5252] text-white"
                  >
                    {createCafeMutation.isPending ? (
                      <Loader2 className="w-4 h-4 animate-spin" />
                    ) : (
                      '등록'
                    )}
                  </Button>
                </DialogFooter>
              </form>
            </DialogContent>
          </Dialog>

          {/* Create Cafe Owner Dialog */}
          <Dialog open={ownerDialogOpen} onOpenChange={setOwnerDialogOpen}>
            <DialogTrigger
              render={
                <Button variant="outline">
                  <Plus className="w-4 h-4" />
                  카페 주인 등록
                </Button>
              }
            />
            <DialogContent>
              <DialogHeader>
                <DialogTitle>카페 주인 등록</DialogTitle>
              </DialogHeader>
              <form
                onSubmit={(e) => {
                  e.preventDefault()
                  createOwnerMutation.mutate({
                    ...ownerForm,
                    cafeId: Number(ownerForm.cafeId),
                  })
                }}
                className="space-y-4"
              >
                <div className="space-y-1.5">
                  <Label htmlFor="ownerName">이름</Label>
                  <Input
                    id="ownerName"
                    value={ownerForm.name}
                    onChange={(e) =>
                      setOwnerForm((p) => ({ ...p, name: e.target.value }))
                    }
                    required
                  />
                </div>
                <div className="space-y-1.5">
                  <Label htmlFor="ownerEmail">이메일</Label>
                  <Input
                    id="ownerEmail"
                    type="email"
                    value={ownerForm.email}
                    onChange={(e) =>
                      setOwnerForm((p) => ({ ...p, email: e.target.value }))
                    }
                    required
                  />
                </div>
                <div className="space-y-1.5">
                  <Label htmlFor="ownerPassword">비밀번호</Label>
                  <Input
                    id="ownerPassword"
                    type="password"
                    value={ownerForm.password}
                    onChange={(e) =>
                      setOwnerForm((p) => ({ ...p, password: e.target.value }))
                    }
                    required
                  />
                </div>
                <div className="space-y-1.5">
                  <Label htmlFor="ownerPhone">전화번호</Label>
                  <Input
                    id="ownerPhone"
                    value={ownerForm.phoneNumber}
                    onChange={(e) =>
                      setOwnerForm((p) => ({ ...p, phoneNumber: e.target.value }))
                    }
                    placeholder="010-0000-0000"
                    required
                  />
                </div>
                <div className="space-y-1.5">
                  <Label>카페 선택</Label>
                  <Select
                    value={ownerForm.cafeId}
                    onValueChange={(v) =>
                      setOwnerForm((p) => ({ ...p, cafeId: v as string }))
                    }
                  >
                    <SelectTrigger className="w-full">
                      <SelectValue placeholder="카페를 선택하세요" />
                    </SelectTrigger>
                    <SelectContent>
                      {cafes?.map((c) => (
                        <SelectItem key={c.id} value={String(c.id)}>
                          {c.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                {createOwnerMutation.isError && (
                  <p className="text-sm text-red-500">등록에 실패했습니다.</p>
                )}
                <DialogFooter>
                  <DialogClose render={<Button variant="outline" />}>
                    취소
                  </DialogClose>
                  <Button
                    type="submit"
                    disabled={createOwnerMutation.isPending}
                    className="bg-[#FF6B6B] hover:bg-[#FF5252] text-white"
                  >
                    {createOwnerMutation.isPending ? (
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
      </div>

      <Card>
        <CardHeader>
          <CardTitle>전체 카페</CardTitle>
        </CardHeader>
        <CardContent>
          {!cafes || cafes.length === 0 ? (
            <EmptyState
              icon={Coffee}
              message="등록된 카페가 없습니다"
              description="카페를 등록해보세요."
            />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>ID</TableHead>
                  <TableHead>카페명</TableHead>
                  <TableHead>주소</TableHead>
                  <TableHead>슬러그</TableHead>
                  <TableHead>상태</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {cafes.map((cafe) => (
                  <TableRow key={cafe.id}>
                    <TableCell>{cafe.id}</TableCell>
                    <TableCell className="font-medium">{cafe.name}</TableCell>
                    <TableCell className="max-w-[200px] truncate">
                      {cafe.address}
                    </TableCell>
                    <TableCell>{cafe.slug}</TableCell>
                    <TableCell>
                      {cafe.isActive ? (
                        <span className="text-green-600 text-sm">활성</span>
                      ) : (
                        <span className="text-gray-400 text-sm">비활성</span>
                      )}
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
