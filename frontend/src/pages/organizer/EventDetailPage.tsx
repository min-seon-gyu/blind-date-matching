import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { organizerApi } from '@/api/organizer'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Badge } from '@/components/ui/badge'
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
import { Loader2, Users, Check, X } from 'lucide-react'
import type { Event, Application } from '@/types'

function appStatusBadge(status: string) {
  switch (status) {
    case 'PENDING':
      return <Badge className="bg-yellow-100 text-yellow-700">대기</Badge>
    case 'APPROVED':
      return <Badge className="bg-green-100 text-green-700">승인</Badge>
    case 'REJECTED':
      return <Badge className="bg-red-100 text-red-700">거절</Badge>
    case 'CANCELLED':
      return <Badge className="bg-gray-100 text-gray-600">취소</Badge>
    case 'COMPLETED':
      return <Badge className="bg-blue-100 text-blue-700">완료</Badge>
    default:
      return <Badge variant="secondary">{status}</Badge>
  }
}

export default function OrganizerEventDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const eventId = Number(id)

  const { data: events } = useQuery<Event[]>({
    queryKey: ['organizer', 'events'],
    queryFn: () => organizerApi.getEvents().then((r) => r.data),
  })

  const event = events?.find((e) => e.id === eventId)

  const { data: applications, isLoading: appsLoading } = useQuery<Application[]>({
    queryKey: ['organizer', 'applications', eventId],
    queryFn: () => organizerApi.getApplications(eventId).then((r) => r.data),
    enabled: !!eventId,
  })

  const [editForm, setEditForm] = useState<Record<string, string>>({})
  const [editing, setEditing] = useState(false)

  const updateMutation = useMutation({
    mutationFn: (data: Record<string, unknown>) =>
      organizerApi.updateEvent(eventId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['organizer', 'events'] })
      setEditing(false)
    },
  })

  const deleteMutation = useMutation({
    mutationFn: () => organizerApi.deleteEvent(eventId),
    onSuccess: () => navigate('/organizer/events'),
  })

  const closeMutation = useMutation({
    mutationFn: () => organizerApi.closeEvent(eventId),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: ['organizer', 'events'] }),
  })

  const approveMutation = useMutation({
    mutationFn: (appId: number) => organizerApi.approveApplication(appId),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ['organizer', 'applications', eventId],
      })
      queryClient.invalidateQueries({ queryKey: ['organizer', 'events'] })
    },
  })

  const rejectMutation = useMutation({
    mutationFn: (appId: number) => organizerApi.rejectApplication(appId),
    onSuccess: () =>
      queryClient.invalidateQueries({
        queryKey: ['organizer', 'applications', eventId],
      }),
  })

  if (!events) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <Loader2 className="w-8 h-8 text-[#FF6B6B] animate-spin" />
      </div>
    )
  }

  if (!event) {
    return (
      <EmptyState
        message="이벤트를 찾을 수 없습니다"
        actionLabel="목록으로"
        onAction={() => navigate('/organizer/events')}
      />
    )
  }

  const startEditing = () => {
    setEditForm({
      title: event.title,
      date: event.date,
      time: event.time,
      price: String(event.price),
      maleCapacity: String(event.maleCapacity),
      femaleCapacity: String(event.femaleCapacity),
      description: event.description ?? '',
      minAge: event.minAge ? String(event.minAge) : '',
      maxAge: event.maxAge ? String(event.maxAge) : '',
      maxChoices: String(event.maxChoices),
    })
    setEditing(true)
  }

  const handleSave = () => {
    updateMutation.mutate({
      title: editForm.title,
      date: editForm.date,
      time: editForm.time,
      price: Number(editForm.price),
      maleCapacity: Number(editForm.maleCapacity),
      femaleCapacity: Number(editForm.femaleCapacity),
      description: editForm.description || null,
      minAge: editForm.minAge ? Number(editForm.minAge) : null,
      maxAge: editForm.maxAge ? Number(editForm.maxAge) : null,
      maxChoices: Number(editForm.maxChoices),
    })
  }

  const approvedCount =
    applications?.filter((a) => a.status === 'APPROVED' || a.status === 'COMPLETED')
      .length ?? 0

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">{event.title}</h1>
        <Badge
          className={
            event.status === 'OPEN'
              ? 'bg-green-100 text-green-700'
              : event.status === 'CLOSED'
                ? 'bg-yellow-100 text-yellow-700'
                : 'bg-gray-100 text-gray-600'
          }
        >
          {event.status === 'OPEN'
            ? '모집 중'
            : event.status === 'CLOSED'
              ? '마감'
              : '완료'}
        </Badge>
      </div>

      <Tabs defaultValue="info">
        <TabsList>
          <TabsTrigger value="info">이벤트 정보</TabsTrigger>
          <TabsTrigger value="applications">신청자 관리</TabsTrigger>
          <TabsTrigger value="stats">통계</TabsTrigger>
        </TabsList>

        {/* Info Tab */}
        <TabsContent value="info">
          <Card>
            <CardContent className="pt-4 space-y-4">
              {editing ? (
                <>
                  <div className="space-y-1.5">
                    <Label>제목</Label>
                    <Input
                      value={editForm.title}
                      onChange={(e) =>
                        setEditForm((p) => ({ ...p, title: e.target.value }))
                      }
                    />
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-1.5">
                      <Label>날짜</Label>
                      <Input
                        type="date"
                        value={editForm.date}
                        onChange={(e) =>
                          setEditForm((p) => ({ ...p, date: e.target.value }))
                        }
                      />
                    </div>
                    <div className="space-y-1.5">
                      <Label>시간</Label>
                      <Input
                        type="time"
                        value={editForm.time}
                        onChange={(e) =>
                          setEditForm((p) => ({ ...p, time: e.target.value }))
                        }
                      />
                    </div>
                  </div>
                  <div className="space-y-1.5">
                    <Label>참가비 (원)</Label>
                    <Input
                      type="number"
                      value={editForm.price}
                      onChange={(e) =>
                        setEditForm((p) => ({ ...p, price: e.target.value }))
                      }
                    />
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-1.5">
                      <Label>남성 정원</Label>
                      <Input
                        type="number"
                        value={editForm.maleCapacity}
                        onChange={(e) =>
                          setEditForm((p) => ({
                            ...p,
                            maleCapacity: e.target.value,
                          }))
                        }
                      />
                    </div>
                    <div className="space-y-1.5">
                      <Label>여성 정원</Label>
                      <Input
                        type="number"
                        value={editForm.femaleCapacity}
                        onChange={(e) =>
                          setEditForm((p) => ({
                            ...p,
                            femaleCapacity: e.target.value,
                          }))
                        }
                      />
                    </div>
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-1.5">
                      <Label>최소 나이</Label>
                      <Input
                        type="number"
                        value={editForm.minAge}
                        onChange={(e) =>
                          setEditForm((p) => ({ ...p, minAge: e.target.value }))
                        }
                      />
                    </div>
                    <div className="space-y-1.5">
                      <Label>최대 나이</Label>
                      <Input
                        type="number"
                        value={editForm.maxAge}
                        onChange={(e) =>
                          setEditForm((p) => ({ ...p, maxAge: e.target.value }))
                        }
                      />
                    </div>
                  </div>
                  <div className="space-y-1.5">
                    <Label>최대 선택 수</Label>
                    <Input
                      type="number"
                      value={editForm.maxChoices}
                      onChange={(e) =>
                        setEditForm((p) => ({ ...p, maxChoices: e.target.value }))
                      }
                    />
                  </div>
                  <div className="space-y-1.5">
                    <Label>설명</Label>
                    <Textarea
                      value={editForm.description}
                      onChange={(e) =>
                        setEditForm((p) => ({ ...p, description: e.target.value }))
                      }
                    />
                  </div>
                  <div className="flex gap-3 pt-2">
                    <Button variant="outline" onClick={() => setEditing(false)}>
                      취소
                    </Button>
                    <Button
                      className="bg-[#FF6B6B] hover:bg-[#FF5252] text-white"
                      onClick={handleSave}
                      disabled={updateMutation.isPending}
                    >
                      {updateMutation.isPending ? (
                        <Loader2 className="w-4 h-4 animate-spin" />
                      ) : (
                        '저장'
                      )}
                    </Button>
                  </div>
                </>
              ) : (
                <>
                  <div className="grid grid-cols-2 gap-y-3 text-sm">
                    <div className="text-gray-500">날짜</div>
                    <div>{event.date}</div>
                    <div className="text-gray-500">시간</div>
                    <div>{event.time}</div>
                    <div className="text-gray-500">참가비</div>
                    <div>{event.price.toLocaleString()}원</div>
                    <div className="text-gray-500">정원 (남/여)</div>
                    <div>
                      {event.maleCapacity}명 / {event.femaleCapacity}명
                    </div>
                    <div className="text-gray-500">현재 인원 (남/여)</div>
                    <div>
                      {event.currentMaleCount}명 / {event.currentFemaleCount}명
                    </div>
                    {event.minAge && (
                      <>
                        <div className="text-gray-500">연령 제한</div>
                        <div>
                          {event.minAge}세 ~ {event.maxAge}세
                        </div>
                      </>
                    )}
                    <div className="text-gray-500">최대 선택 수</div>
                    <div>{event.maxChoices}명</div>
                  </div>
                  {event.description && (
                    <div className="pt-2">
                      <p className="text-sm text-gray-500 mb-1">설명</p>
                      <p className="text-sm">{event.description}</p>
                    </div>
                  )}
                  <div className="flex gap-3 pt-4">
                    <Button variant="outline" onClick={startEditing}>
                      수정
                    </Button>
                    {event.status === 'OPEN' && (
                      <Button
                        variant="secondary"
                        onClick={() => closeMutation.mutate()}
                        disabled={closeMutation.isPending}
                      >
                        마감하기
                      </Button>
                    )}
                    <Button
                      variant="destructive"
                      onClick={() => {
                        if (confirm('정말 삭제하시겠습니까?'))
                          deleteMutation.mutate()
                      }}
                      disabled={deleteMutation.isPending}
                    >
                      삭제
                    </Button>
                  </div>
                </>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        {/* Applications Tab */}
        <TabsContent value="applications">
          <Card>
            <CardHeader>
              <CardTitle>신청자 목록</CardTitle>
            </CardHeader>
            <CardContent>
              {appsLoading ? (
                <div className="flex justify-center py-8">
                  <Loader2 className="w-6 h-6 text-[#FF6B6B] animate-spin" />
                </div>
              ) : !applications || applications.length === 0 ? (
                <EmptyState
                  icon={Users}
                  message="신청자가 없습니다"
                  description="아직 참가 신청이 없습니다."
                />
              ) : (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>이름</TableHead>
                      <TableHead>상태</TableHead>
                      <TableHead>신청일</TableHead>
                      <TableHead>관리</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {applications.map((app) => (
                      <TableRow key={app.id}>
                        <TableCell className="font-medium">
                          참가자 #{app.participantId}
                        </TableCell>
                        <TableCell>{appStatusBadge(app.status)}</TableCell>
                        <TableCell>
                          {new Date(app.appliedAt).toLocaleDateString('ko-KR')}
                        </TableCell>
                        <TableCell>
                          {app.status === 'PENDING' && (
                            <div className="flex gap-2">
                              <Button
                                size="xs"
                                className="bg-green-500 hover:bg-green-600 text-white"
                                onClick={() => approveMutation.mutate(app.id)}
                                disabled={approveMutation.isPending}
                              >
                                <Check className="w-3 h-3" />
                                승인
                              </Button>
                              <Button
                                size="xs"
                                variant="destructive"
                                onClick={() => rejectMutation.mutate(app.id)}
                                disabled={rejectMutation.isPending}
                              >
                                <X className="w-3 h-3" />
                                거절
                              </Button>
                            </div>
                          )}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        {/* Stats Tab */}
        <TabsContent value="stats">
          <Card>
            <CardHeader>
              <CardTitle>통계</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
                <div className="text-center p-4 bg-gray-50 rounded-lg">
                  <p className="text-2xl font-bold text-[#FF6B6B]">
                    {applications?.length ?? 0}
                  </p>
                  <p className="text-sm text-gray-500 mt-1">총 신청자</p>
                </div>
                <div className="text-center p-4 bg-gray-50 rounded-lg">
                  <p className="text-2xl font-bold text-green-600">
                    {approvedCount}
                  </p>
                  <p className="text-sm text-gray-500 mt-1">승인된 참가자</p>
                </div>
                <div className="text-center p-4 bg-gray-50 rounded-lg">
                  <p className="text-2xl font-bold text-blue-600">
                    {event.currentMaleCount + event.currentFemaleCount}
                  </p>
                  <p className="text-sm text-gray-500 mt-1">현재 인원</p>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}
