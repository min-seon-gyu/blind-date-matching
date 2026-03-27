import { useState, useEffect } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { cafeOwnerApi } from '@/api/cafeOwner'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import KakaoMap from '@/components/common/KakaoMap'
import { Loader2, Save } from 'lucide-react'
import type { Cafe } from '@/types'

export default function MyCafePage() {
  const queryClient = useQueryClient()

  const { data: cafe, isLoading } = useQuery<Cafe>({
    queryKey: ['cafeOwner', 'myCafe'],
    queryFn: () => cafeOwnerApi.getMyCafe().then((r) => r.data),
  })

  const [form, setForm] = useState({
    name: '',
    address: '',
    description: '',
    logoUrl: '',
    coverImageUrl: '',
    latitude: '',
    longitude: '',
  })

  const [addressSearch, setAddressSearch] = useState('')

  useEffect(() => {
    if (cafe) {
      setForm({
        name: cafe.name ?? '',
        address: cafe.address ?? '',
        description: cafe.description ?? '',
        logoUrl: cafe.logoUrl ?? '',
        coverImageUrl: cafe.coverImageUrl ?? '',
        latitude: cafe.latitude ? String(cafe.latitude) : '',
        longitude: cafe.longitude ? String(cafe.longitude) : '',
      })
    }
  }, [cafe])

  const mutation = useMutation({
    mutationFn: (data: Record<string, unknown>) => cafeOwnerApi.updateMyCafe(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['cafeOwner', 'myCafe'] })
    },
  })

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault()
    mutation.mutate({
      name: form.name,
      address: form.address,
      description: form.description || null,
      logoUrl: form.logoUrl || null,
      coverImageUrl: form.coverImageUrl || null,
      latitude: form.latitude ? Number(form.latitude) : null,
      longitude: form.longitude ? Number(form.longitude) : null,
    })
  }

  const handleAddressSearch = async () => {
    if (!addressSearch.trim()) return
    // Use Kakao address search API (geocoding) if available
    try {
      const res = await fetch(
        `https://dapi.kakao.com/v2/local/search/address.json?query=${encodeURIComponent(addressSearch)}`,
        {
          headers: {
            Authorization: `KakaoAK ${import.meta.env.VITE_KAKAO_REST_API_KEY || ''}`,
          },
        }
      )
      const data = await res.json()
      if (data.documents?.[0]) {
        const doc = data.documents[0]
        setForm((prev) => ({
          ...prev,
          address: doc.address_name || addressSearch,
          latitude: doc.y,
          longitude: doc.x,
        }))
      }
    } catch {
      // Fallback: just set address text
      setForm((prev) => ({ ...prev, address: addressSearch }))
    }
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <Loader2 className="w-8 h-8 text-[#FF6B6B] animate-spin" />
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">내 카페 관리</h1>

      <Card>
        <CardHeader>
          <CardTitle>카페 정보</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSave} className="space-y-4">
            <div className="space-y-1.5">
              <Label htmlFor="name">카페명</Label>
              <Input
                id="name"
                value={form.name}
                onChange={(e) => setForm((p) => ({ ...p, name: e.target.value }))}
                required
              />
            </div>

            <div className="space-y-1.5">
              <Label>주소 검색</Label>
              <div className="flex gap-2">
                <Input
                  value={addressSearch}
                  onChange={(e) => setAddressSearch(e.target.value)}
                  placeholder="주소를 검색하세요"
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') {
                      e.preventDefault()
                      handleAddressSearch()
                    }
                  }}
                />
                <Button
                  type="button"
                  variant="outline"
                  onClick={handleAddressSearch}
                >
                  검색
                </Button>
              </div>
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="address">주소</Label>
              <Input
                id="address"
                value={form.address}
                onChange={(e) =>
                  setForm((p) => ({ ...p, address: e.target.value }))
                }
                required
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <Label htmlFor="lat">위도</Label>
                <Input
                  id="lat"
                  type="number"
                  step="any"
                  value={form.latitude}
                  onChange={(e) =>
                    setForm((p) => ({ ...p, latitude: e.target.value }))
                  }
                />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="lng">경도</Label>
                <Input
                  id="lng"
                  type="number"
                  step="any"
                  value={form.longitude}
                  onChange={(e) =>
                    setForm((p) => ({ ...p, longitude: e.target.value }))
                  }
                />
              </div>
            </div>

            {/* Map Preview */}
            <div className="space-y-1.5">
              <Label>지도 미리보기</Label>
              <KakaoMap
                latitude={form.latitude ? Number(form.latitude) : null}
                longitude={form.longitude ? Number(form.longitude) : null}
                name={form.name || '내 카페'}
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
                placeholder="카페 설명"
              />
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="logoUrl">로고 URL</Label>
              <Input
                id="logoUrl"
                value={form.logoUrl}
                onChange={(e) =>
                  setForm((p) => ({ ...p, logoUrl: e.target.value }))
                }
                placeholder="https://..."
              />
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="coverImageUrl">커버 이미지 URL</Label>
              <Input
                id="coverImageUrl"
                value={form.coverImageUrl}
                onChange={(e) =>
                  setForm((p) => ({ ...p, coverImageUrl: e.target.value }))
                }
                placeholder="https://..."
              />
            </div>

            {mutation.isSuccess && (
              <p className="text-sm text-green-600">저장되었습니다.</p>
            )}
            {mutation.isError && (
              <p className="text-sm text-red-500">저장에 실패했습니다.</p>
            )}

            <Button
              type="submit"
              disabled={mutation.isPending}
              className="bg-[#FF6B6B] hover:bg-[#FF5252] text-white"
            >
              {mutation.isPending ? (
                <Loader2 className="w-4 h-4 animate-spin" />
              ) : (
                <>
                  <Save className="w-4 h-4" />
                  저장하기
                </>
              )}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
