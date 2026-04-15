import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery, useMutation } from '@tanstack/react-query'
import { organizerApi } from '@/api/organizer'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import EmptyState from '@/components/common/EmptyState'
import { Loader2, Handshake } from 'lucide-react'
import type { Partnership } from '@/types'

export default function OrganizerEventCreatePage() {
  const navigate = useNavigate()

  const { data: partnerships, isLoading: partLoading } = useQuery<Partnership[]>({
    queryKey: ['organizer', 'partnerships'],
    queryFn: () => organizerApi.getPartnerships().then((r) => r.data),
  })

  const activePartnerships = partnerships?.filter((p) => p.status === 'ACTIVE') ?? []

  const [form, setForm] = useState({
    cafeId: '',
    title: '',
    date: '',
    time: '',
    price: '',
    maleCapacity: '',
    femaleCapacity: '',
    description: '',
    minAge: '',
    maxAge: '',
    maxChoices: '3',
  })

  const mutation = useMutation({
    mutationFn: (data: Record<string, unknown>) => organizerApi.createEvent(data),
    onSuccess: () => navigate('/organizer/events'),
  })

  const handleChange = (field: string, value: string) => {
    setForm((prev) => ({ ...prev, [field]: value }))
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    mutation.mutate({
      cafeId: Number(form.cafeId),
      title: form.title,
      date: form.date,
      time: form.time,
      price: Number(form.price),
      maleCapacity: Number(form.maleCapacity),
      femaleCapacity: Number(form.femaleCapacity),
      description: form.description || null,
      minAge: form.minAge ? Number(form.minAge) : null,
      maxAge: form.maxAge ? Number(form.maxAge) : null,
      maxChoices: Number(form.maxChoices),
    })
  }

  if (partLoading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <Loader2 className="w-8 h-8 text-[#FF6B6B] animate-spin" />
      </div>
    )
  }

  if (activePartnerships.length === 0) {
    return (
      <div className="space-y-6">
        <h1 className="text-2xl font-bold">이벤트 생성</h1>
        <EmptyState
          icon={Handshake}
          message="먼저 마켓플레이스에서 카페와 제휴하세요"
          description="이벤트를 생성하려면 제휴된 카페가 필요합니다."
          actionLabel="마켓플레이스 가기"
          onAction={() => navigate('/marketplace')}
        />
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">이벤트 생성</h1>

      <Card>
        <CardHeader>
          <CardTitle>이벤트 정보 입력</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-1.5">
              <Label>제휴 카페 선택</Label>
              <Select
                value={form.cafeId}
                onValueChange={(v) => handleChange('cafeId', v as string)}
              >
                <SelectTrigger className="w-full">
                  <SelectValue placeholder="카페를 선택하세요" />
                </SelectTrigger>
                <SelectContent>
                  {activePartnerships.map((p) => (
                    <SelectItem key={p.cafeId} value={String(p.cafeId)}>
                      {p.cafeName}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="title">제목</Label>
              <Input
                id="title"
                value={form.title}
                onChange={(e) => handleChange('title', e.target.value)}
                placeholder="이벤트 제목"
                required
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <Label htmlFor="date">날짜</Label>
                <Input
                  id="date"
                  type="date"
                  value={form.date}
                  onChange={(e) => handleChange('date', e.target.value)}
                  required
                />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="time">시간</Label>
                <Input
                  id="time"
                  type="time"
                  value={form.time}
                  onChange={(e) => handleChange('time', e.target.value)}
                  required
                />
              </div>
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="price">참가비 (원)</Label>
              <Input
                id="price"
                type="number"
                value={form.price}
                onChange={(e) => handleChange('price', e.target.value)}
                placeholder="30000"
                required
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <Label htmlFor="maleCapacity">남성 정원</Label>
                <Input
                  id="maleCapacity"
                  type="number"
                  value={form.maleCapacity}
                  onChange={(e) => handleChange('maleCapacity', e.target.value)}
                  placeholder="5"
                  required
                />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="femaleCapacity">여성 정원</Label>
                <Input
                  id="femaleCapacity"
                  type="number"
                  value={form.femaleCapacity}
                  onChange={(e) => handleChange('femaleCapacity', e.target.value)}
                  placeholder="5"
                  required
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <Label htmlFor="minAge">최소 나이</Label>
                <Input
                  id="minAge"
                  type="number"
                  value={form.minAge}
                  onChange={(e) => handleChange('minAge', e.target.value)}
                  placeholder="20"
                />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="maxAge">최대 나이</Label>
                <Input
                  id="maxAge"
                  type="number"
                  value={form.maxAge}
                  onChange={(e) => handleChange('maxAge', e.target.value)}
                  placeholder="35"
                />
              </div>
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="maxChoices">최대 선택 수</Label>
              <Input
                id="maxChoices"
                type="number"
                value={form.maxChoices}
                onChange={(e) => handleChange('maxChoices', e.target.value)}
                placeholder="3"
                required
              />
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="description">설명</Label>
              <Textarea
                id="description"
                value={form.description}
                onChange={(e) => handleChange('description', e.target.value)}
                placeholder="이벤트 설명을 입력하세요"
              />
            </div>

            {mutation.isError && (
              <p className="text-sm text-red-500">이벤트 생성에 실패했습니다.</p>
            )}

            <div className="flex gap-3 pt-2">
              <Button
                type="button"
                variant="outline"
                onClick={() => navigate('/organizer/events')}
              >
                취소
              </Button>
              <Button
                type="submit"
                disabled={mutation.isPending}
                className="bg-[#FF6B6B] hover:bg-[#FF5252] text-white"
              >
                {mutation.isPending ? (
                  <Loader2 className="w-4 h-4 animate-spin" />
                ) : (
                  '생성하기'
                )}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
