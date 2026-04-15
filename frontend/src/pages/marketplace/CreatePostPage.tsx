import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { marketplaceApi } from '@/api/marketplace'
import { useAuthStore } from '@/stores/authStore'
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
import { Loader2 } from 'lucide-react'

const REGIONS = [
  '서울',
  '경기',
  '인천',
  '부산',
  '대구',
  '광주',
  '대전',
  '울산',
  '세종',
  '강원',
  '충북',
  '충남',
  '전북',
  '전남',
  '경북',
  '경남',
  '제주',
]

export default function CreatePostPage() {
  const navigate = useNavigate()
  const { user } = useAuthStore()

  const isCafeOwner = user?.userType === 'CAFE_OWNER'
  const postType = isCafeOwner ? 'OFFER_SPACE' : 'SEEK_SPACE'

  const [form, setForm] = useState({
    title: '',
    description: '',
    region: '',
    capacity: '',
    preferredDate: '',
    imageUrl: '',
  })

  const mutation = useMutation({
    mutationFn: (data: Record<string, unknown>) => marketplaceApi.createPost(data),
    onSuccess: () => navigate('/marketplace'),
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    mutation.mutate({
      type: postType,
      title: form.title,
      description: form.description,
      region: form.region,
      capacity: form.capacity ? Number(form.capacity) : null,
      preferredDate: form.preferredDate || null,
      imageUrls: form.imageUrl ? [form.imageUrl] : [],
    })
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">글 작성</h1>

      <Card>
        <CardHeader>
          <CardTitle>
            {postType === 'OFFER_SPACE' ? '장소 제공' : '장소 구함'} 글 작성
          </CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-1.5">
              <Label htmlFor="title">제목</Label>
              <Input
                id="title"
                value={form.title}
                onChange={(e) => setForm((p) => ({ ...p, title: e.target.value }))}
                placeholder="제목을 입력하세요"
                required
              />
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="description">설명</Label>
              <Textarea
                id="description"
                value={form.description}
                onChange={(e) =>
                  setForm((p) => ({ ...p, description: e.target.value }))
                }
                placeholder="상세 설명을 입력하세요"
                required
              />
            </div>

            <div className="space-y-1.5">
              <Label>지역</Label>
              <Select
                value={form.region}
                onValueChange={(v) => setForm((p) => ({ ...p, region: v as string }))}
              >
                <SelectTrigger className="w-full">
                  <SelectValue placeholder="지역을 선택하세요" />
                </SelectTrigger>
                <SelectContent>
                  {REGIONS.map((r) => (
                    <SelectItem key={r} value={r}>
                      {r}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {postType === 'OFFER_SPACE' && (
              <div className="space-y-1.5">
                <Label htmlFor="capacity">수용 인원</Label>
                <Input
                  id="capacity"
                  type="number"
                  value={form.capacity}
                  onChange={(e) =>
                    setForm((p) => ({ ...p, capacity: e.target.value }))
                  }
                  placeholder="10"
                />
              </div>
            )}

            <div className="space-y-1.5">
              <Label htmlFor="preferredDate">희망 날짜</Label>
              <Input
                id="preferredDate"
                type="date"
                value={form.preferredDate}
                onChange={(e) =>
                  setForm((p) => ({ ...p, preferredDate: e.target.value }))
                }
              />
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="imageUrl">이미지 URL</Label>
              <Input
                id="imageUrl"
                value={form.imageUrl}
                onChange={(e) =>
                  setForm((p) => ({ ...p, imageUrl: e.target.value }))
                }
                placeholder="https://..."
              />
            </div>

            {mutation.isError && (
              <p className="text-sm text-red-500">글 작성에 실패했습니다.</p>
            )}

            <div className="flex gap-3 pt-2">
              <Button
                type="button"
                variant="outline"
                onClick={() => navigate('/marketplace')}
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
                  '작성하기'
                )}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
