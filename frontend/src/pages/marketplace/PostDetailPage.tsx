import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { marketplaceApi } from '@/api/marketplace'
import { useAuthStore } from '@/stores/authStore'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Textarea } from '@/components/ui/textarea'
import { Label } from '@/components/ui/label'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  DialogClose,
} from '@/components/ui/dialog'
import KakaoMap from '@/components/common/KakaoMap'
import EmptyState from '@/components/common/EmptyState'
import { Loader2, MapPin, Calendar, Users, Pencil, Trash2 } from 'lucide-react'
import type { MarketplacePost } from '@/types'

export default function PostDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { user } = useAuthStore()
  const postId = Number(id)

  const { data: post, isLoading } = useQuery<MarketplacePost>({
    queryKey: ['marketplace', 'post', postId],
    queryFn: () => marketplaceApi.getPost(postId).then((r) => r.data),
    enabled: !!postId,
  })

  const [partnershipDialogOpen, setPartnershipDialogOpen] = useState(false)
  const [partnershipMessage, setPartnershipMessage] = useState('')

  const requestMutation = useMutation({
    mutationFn: ({ postId, message }: { postId: number; message?: string }) =>
      marketplaceApi.requestPartnership(postId, message),
    onSuccess: () => {
      setPartnershipDialogOpen(false)
      setPartnershipMessage('')
    },
  })

  const deleteMutation = useMutation({
    mutationFn: (id: number) => marketplaceApi.deletePost(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['marketplace'] })
      navigate('/marketplace')
    },
  })

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

  const isAuthor = user?.id === post.authorId
  const isOppositeRole =
    (user?.userType === 'ORGANIZER' && post.authorType === 'CAFE_OWNER') ||
    (user?.userType === 'CAFE_OWNER' && post.authorType === 'ORGANIZER')

  return (
    <div className="space-y-6">
      <Button variant="ghost" onClick={() => navigate('/marketplace')}>
        ← 목록으로
      </Button>

      <Card>
        <CardHeader>
          <div className="flex items-start justify-between gap-3">
            <div>
              <CardTitle className="text-xl">{post.title}</CardTitle>
              <p className="text-sm text-gray-500 mt-1">
                작성자: {post.authorName}
              </p>
            </div>
            <Badge
              className={
                post.type === 'OFFER_SPACE'
                  ? 'bg-blue-100 text-blue-700'
                  : 'bg-orange-100 text-orange-700'
              }
            >
              {post.type === 'OFFER_SPACE' ? '장소 제공' : '장소 구함'}
            </Badge>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          <p className="text-sm whitespace-pre-wrap">{post.description}</p>

          <div className="grid grid-cols-2 gap-y-2 text-sm">
            <div className="flex items-center gap-2 text-gray-500">
              <MapPin className="w-4 h-4" />
              지역
            </div>
            <div>{post.region}</div>

            {post.capacity && (
              <>
                <div className="flex items-center gap-2 text-gray-500">
                  <Users className="w-4 h-4" />
                  수용 인원
                </div>
                <div>{post.capacity}명</div>
              </>
            )}

            {post.preferredDate && (
              <>
                <div className="flex items-center gap-2 text-gray-500">
                  <Calendar className="w-4 h-4" />
                  희망 날짜
                </div>
                <div>{post.preferredDate}</div>
              </>
            )}
          </div>

          {/* Images */}
          {post.imageUrls && post.imageUrls.length > 0 && (
            <div className="space-y-2">
              <p className="text-sm font-medium text-gray-500">이미지</p>
              <div className="grid grid-cols-2 gap-2">
                {post.imageUrls.map((url, i) => (
                  <img
                    key={i}
                    src={url}
                    alt={`이미지 ${i + 1}`}
                    className="w-full h-40 object-cover rounded-lg"
                  />
                ))}
              </div>
            </div>
          )}

          {/* Map for OFFER_SPACE */}
          {post.type === 'OFFER_SPACE' && (post.cafeLatitude || post.cafeLongitude) && (
            <div className="space-y-2">
              <p className="text-sm font-medium text-gray-500">위치</p>
              {post.cafeAddress && (
                <p className="text-sm text-gray-600">{post.cafeAddress}</p>
              )}
              <KakaoMap
                latitude={post.cafeLatitude}
                longitude={post.cafeLongitude}
                name={post.authorName}
              />
            </div>
          )}

          {/* Actions */}
          <div className="flex gap-3 pt-4">
            {isAuthor && (
              <>
                <Button
                  variant="outline"
                  onClick={() => navigate(`/marketplace/${post.id}/edit`)}
                >
                  <Pencil className="w-4 h-4" />
                  수정
                </Button>
                <Button
                  variant="destructive"
                  onClick={() => {
                    if (confirm('정말 삭제하시겠습니까?'))
                      deleteMutation.mutate(post.id)
                  }}
                  disabled={deleteMutation.isPending}
                >
                  <Trash2 className="w-4 h-4" />
                  삭제
                </Button>
              </>
            )}

            {isOppositeRole && (
              <>
                <Button
                  className="bg-[#FF6B6B] hover:bg-[#FF5252] text-white"
                  onClick={() => setPartnershipDialogOpen(true)}
                >
                  제휴 요청
                </Button>

                <Dialog
                  open={partnershipDialogOpen}
                  onOpenChange={setPartnershipDialogOpen}
                >
                  <DialogContent>
                    <DialogHeader>
                      <DialogTitle>제휴 요청</DialogTitle>
                    </DialogHeader>
                    <div className="space-y-4">
                      <div className="space-y-1.5">
                        <Label>메시지 (선택)</Label>
                        <Textarea
                          value={partnershipMessage}
                          onChange={(e) =>
                            setPartnershipMessage(e.target.value)
                          }
                          placeholder="제휴 요청 메시지를 입력하세요"
                        />
                      </div>
                      {requestMutation.isError && (
                        <p className="text-sm text-red-500">
                          요청에 실패했습니다.
                        </p>
                      )}
                      {requestMutation.isSuccess && (
                        <p className="text-sm text-green-600">
                          요청이 전송되었습니다.
                        </p>
                      )}
                    </div>
                    <DialogFooter>
                      <DialogClose render={<Button variant="outline" />}>
                        취소
                      </DialogClose>
                      <Button
                        className="bg-[#FF6B6B] hover:bg-[#FF5252] text-white"
                        onClick={() =>
                          requestMutation.mutate({
                            postId: post.id,
                            message: partnershipMessage || undefined,
                          })
                        }
                        disabled={requestMutation.isPending}
                      >
                        {requestMutation.isPending ? (
                          <Loader2 className="w-4 h-4 animate-spin" />
                        ) : (
                          '요청 보내기'
                        )}
                      </Button>
                    </DialogFooter>
                  </DialogContent>
                </Dialog>
              </>
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
