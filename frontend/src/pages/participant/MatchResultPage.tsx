import { useParams, useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { getMatchResult } from '@/api/event'
import { Button } from '@/components/ui/button'
import { ArrowLeft, Loader2 } from 'lucide-react'

export default function MatchResultPage() {
  const { slug, eventId } = useParams<{ slug: string; eventId: string }>()
  const navigate = useNavigate()

  const { data: result, isLoading, isError } = useQuery({
    queryKey: ['matchResult', eventId],
    queryFn: () => getMatchResult(Number(eventId)),
    enabled: !!eventId,
  })

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <Loader2 className="w-8 h-8 text-[#FF6B6B] animate-spin" />
      </div>
    )
  }

  return (
    <div className="pb-6">
      {/* Header */}
      <div className="sticky top-0 z-10 bg-[#FFF5F5] px-4 py-3 flex items-center gap-3">
        <button
          onClick={() => navigate(-1)}
          className="w-10 h-10 flex items-center justify-center rounded-xl hover:bg-white/60 transition-colors"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>
        <h1 className="text-lg font-bold">매칭 결과</h1>
      </div>

      <div className="px-4 flex flex-col items-center justify-center min-h-[60vh]">
        {isError ? (
          <div className="text-center">
            <span className="text-5xl block mb-4">😢</span>
            <p className="text-lg font-bold text-gray-700">결과를 불러올 수 없습니다</p>
            <p className="mt-2 text-sm text-gray-400">아직 결과가 발표되지 않았을 수 있어요.</p>
            <Button
              onClick={() => navigate(`/cafes/${slug}/events/${eventId}`)}
              variant="outline"
              className="mt-6 rounded-xl"
            >
              이벤트로 돌아가기
            </Button>
          </div>
        ) : result?.matched ? (
          <div className="text-center">
            {/* Celebration */}
            <div className="relative mb-6">
              <span className="text-7xl block animate-bounce">🎉</span>
            </div>
            <h2 className="text-2xl font-bold text-gray-900 mb-2">
              매칭 성공!
            </h2>
            <p className="text-gray-500 mb-6">
              축하해요! 상대방과 매칭되었습니다
            </p>

            {/* Partner Info */}
            <div className="bg-white rounded-2xl p-6 shadow-sm w-full max-w-xs mx-auto">
              <div className="w-16 h-16 rounded-full bg-gradient-to-br from-[#FF6B6B] to-[#FFE66D] flex items-center justify-center mx-auto mb-3">
                <span className="text-2xl">💕</span>
              </div>
              <p className="text-lg font-bold text-gray-900">
                {result.partnerNickname}
              </p>
              <p className="text-sm text-gray-400 mt-1">
                곧 연락처가 전달될 예정이에요
              </p>
            </div>

            <Button
              onClick={() => navigate('/me/applications')}
              className="mt-8 rounded-xl bg-[#FF6B6B] hover:bg-[#FF5252] text-white"
            >
              신청 내역으로 돌아가기
            </Button>
          </div>
        ) : (
          <div className="text-center">
            <span className="text-5xl block mb-4">💙</span>
            <h2 className="text-2xl font-bold text-gray-900 mb-2">
              아쉽지만 이번엔...
            </h2>
            <p className="text-gray-500 mb-2">
              이번에는 매칭되지 않았어요
            </p>
            <p className="text-sm text-gray-400">
              다음에 더 좋은 인연이 기다리고 있을 거예요!
            </p>

            <Button
              onClick={() => navigate('/me/applications')}
              variant="outline"
              className="mt-8 rounded-xl"
            >
              신청 내역으로 돌아가기
            </Button>
          </div>
        )}
      </div>
    </div>
  )
}
