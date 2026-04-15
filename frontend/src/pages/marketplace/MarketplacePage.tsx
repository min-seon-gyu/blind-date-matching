import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { marketplaceApi } from '@/api/marketplace'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import EmptyState from '@/components/common/EmptyState'
import { Loader2, Plus, Store } from 'lucide-react'
import type { MarketplacePost } from '@/types'

const REGIONS = [
  '전체',
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

function PostCard({ post, onClick }: { post: MarketplacePost; onClick: () => void }) {
  return (
    <Card
      className="cursor-pointer hover:ring-2 hover:ring-[#FF6B6B]/30 transition-all"
      onClick={onClick}
    >
      <CardHeader>
        <div className="flex items-start justify-between gap-2">
          <CardTitle className="line-clamp-1">{post.title}</CardTitle>
          <Badge
            className={
              post.type === 'OFFER_SPACE'
                ? 'bg-blue-100 text-blue-700 shrink-0'
                : 'bg-orange-100 text-orange-700 shrink-0'
            }
          >
            {post.type === 'OFFER_SPACE' ? '장소 제공' : '장소 구함'}
          </Badge>
        </div>
      </CardHeader>
      <CardContent>
        <p className="text-sm text-gray-500 line-clamp-2 mb-3">
          {post.description}
        </p>
        <div className="flex items-center justify-between text-xs text-gray-400">
          <div className="flex items-center gap-2">
            <span>{post.region}</span>
            {post.capacity && <span>· {post.capacity}명 수용</span>}
          </div>
          <span>{post.authorName}</span>
        </div>
      </CardContent>
    </Card>
  )
}

export default function MarketplacePage() {
  const navigate = useNavigate()
  const [tab, setTab] = useState('all')
  const [region, setRegion] = useState('전체')

  const typeParam =
    tab === 'offer' ? 'OFFER_SPACE' : tab === 'seek' ? 'SEEK_SPACE' : undefined
  const regionParam = region === '전체' ? undefined : region

  const { data: posts, isLoading } = useQuery<MarketplacePost[]>({
    queryKey: ['marketplace', 'posts', typeParam, regionParam],
    queryFn: () =>
      marketplaceApi
        .getPosts({ type: typeParam, region: regionParam })
        .then((r) => r.data),
  })

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">마켓플레이스</h1>
        <Button
          className="bg-[#FF6B6B] hover:bg-[#FF5252] text-white"
          onClick={() => navigate('/marketplace/new')}
        >
          <Plus className="w-4 h-4" />
          글 작성
        </Button>
      </div>

      <div className="flex flex-col sm:flex-row sm:items-center gap-4">
        <Tabs
          value={tab}
          onValueChange={(v) => setTab(v as string)}
        >
          <TabsList>
            <TabsTrigger value="all">전체</TabsTrigger>
            <TabsTrigger value="offer">장소 제공</TabsTrigger>
            <TabsTrigger value="seek">장소 구함</TabsTrigger>
          </TabsList>

          {/* hidden tab contents - we use cards below */}
          <TabsContent value="all" className="hidden" />
          <TabsContent value="offer" className="hidden" />
          <TabsContent value="seek" className="hidden" />
        </Tabs>

        <Select value={region} onValueChange={(v) => setRegion(v as string)}>
          <SelectTrigger className="w-32">
            <SelectValue placeholder="지역" />
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

      {isLoading ? (
        <div className="flex items-center justify-center min-h-[40vh]">
          <Loader2 className="w-8 h-8 text-[#FF6B6B] animate-spin" />
        </div>
      ) : !posts || posts.length === 0 ? (
        <EmptyState
          icon={Store}
          message="게시글이 없습니다"
          description="첫 번째 글을 작성해보세요."
          actionLabel="글 작성"
          onAction={() => navigate('/marketplace/new')}
        />
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {posts.map((post) => (
            <PostCard
              key={post.id}
              post={post}
              onClick={() => navigate(`/marketplace/${post.id}`)}
            />
          ))}
        </div>
      )}
    </div>
  )
}
