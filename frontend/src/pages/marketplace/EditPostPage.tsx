import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { marketplaceApi } from '@/api/marketplace'
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
import { Loader2 } from 'lucide-react'
import type { MarketplacePost } from '@/types'

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

export default function EditPostPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const postId = Number(id)

  const { data: post, isLoading } = useQuery<MarketplacePost>({
    queryKey: ['marketplace', 'post', postId],
    queryFn: () => marketplaceApi.getPost(postId).then((r) => r.data),
    enabled: !!postId,
  })

  const [form, setForm] = useState({
    title: '',
    description: '',
    region: '',
    capacity: '',
    preferredDate: '',
    imageUrl: '',
  })

  useEffect(() => {
    if (post) {
      setForm({
        title: post.title,
        description: post.description,
        region: post.region,
        capacity: post.capacity ? String(post.capacity) : '',
        preferredDate: post.preferredDate ?? '',
        imageUrl: post.imageUrls?.[0] ?? '',
      })
    }
  }, [post])

  const mutation = useMutation({
    mutationFn: (data: Record<string, unknown>) =>
      marketplaceApi.updatePost(postId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['marketplace', 'post', postId] })
      navigate(`/marketplace/${postId}`)
    },
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    mutation.mutate({
      title: form.title,
      description: form.description,
      region: form.region,
      capacity: form.capacity ? Number(form.capacity) : null,
      preferredDate: form.preferredDate || null,
      imageUrls: form.imageUrl ? [form.imageUrl] : [],
    })
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <Loader2 className="w-8 h-8 text-[#FF6B6B] animate-spin" />
      </div>
    )
  }

  if (!post) {
    return (
      <EmptyState
        message="게시글을 찾을 수 없습니다"
        actionLabel="목록으로"
        onAction={() => navigate('/marketplace')}
      />
    )
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">글 수정</h1>

      <Card>
        <CardHeader>
          <CardTitle>게시글 수정</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-1.5">
              <Label htmlFor="title">제목</Label>
              <Input
                id="title"
                value={form.title}
                onChange={(e) => setForm((p) => ({ ...p, title: e.target.value }))}
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

            {post.type === 'OFFER_SPACE' && (
              <div className="space-y-1.5">
                <Label htmlFor="capacity">수용 인원</Label>
                <Input
                  id="capacity"
                  type="number"
                  value={form.capacity}
                  onChange={(e) =>
                    setForm((p) => ({ ...p, capacity: e.target.value }))
                  }
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
              <p className="text-sm text-red-500">수정에 실패했습니다.</p>
            )}

            <div className="flex gap-3 pt-2">
              <Button
                type="button"
                variant="outline"
                onClick={() => navigate(`/marketplace/${postId}`)}
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
                  '수정하기'
                )}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
