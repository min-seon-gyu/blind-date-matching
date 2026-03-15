import { useParams, useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { getMatchResult } from '../api/matching'
import Button from '../components/common/Button'

const MatchResultPage = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const eventId = Number(id)

  const { data: result, isLoading } = useQuery({
    queryKey: ['matchResult', eventId],
    queryFn: () => getMatchResult(eventId),
  })

  if (isLoading) {
    return (
      <div
        style={{
          minHeight: '100vh',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          gap: 16,
        }}
      >
        <div style={{ fontSize: 48 }}>💕</div>
        <p style={{ color: 'var(--text-light)', fontSize: 16 }}>결과를 불러오는 중...</p>
      </div>
    )
  }

  if (!result) {
    return (
      <div style={{ textAlign: 'center', padding: 60 }}>
        <p style={{ color: 'var(--text-light)' }}>결과를 불러올 수 없습니다.</p>
      </div>
    )
  }

  return (
    <div
      style={{
        minHeight: '100vh',
        background: result.matched ? 'var(--gradient)' : '#f9f9f9',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '40px 24px',
        textAlign: 'center',
      }}
    >
      {result.matched ? (
        <>
          {/* Matched */}
          <div style={{ fontSize: 80, marginBottom: 20, animation: 'bounce 0.8s ease infinite alternate' }}>
            💕
          </div>
          <style>{`@keyframes bounce { from { transform: scale(1); } to { transform: scale(1.1); } }`}</style>

          <h1
            style={{
              fontSize: 30,
              fontWeight: 800,
              color: '#fff',
              letterSpacing: '-0.5px',
              marginBottom: 8,
            }}
          >
            매칭 성공!
          </h1>
          <p style={{ fontSize: 16, color: 'rgba(255,255,255,0.9)', marginBottom: 32 }}>
            서로 마음이 통했어요 🎉
          </p>

          <div
            style={{
              background: 'rgba(255,255,255,0.2)',
              backdropFilter: 'blur(10px)',
              borderRadius: 20,
              padding: '28px 36px',
              marginBottom: 32,
              border: '1px solid rgba(255,255,255,0.3)',
            }}
          >
            <p style={{ color: 'rgba(255,255,255,0.85)', fontSize: 14, marginBottom: 8 }}>
              매칭된 상대방
            </p>
            <div
              style={{
                width: 72,
                height: 72,
                background: 'rgba(255,255,255,0.3)',
                borderRadius: '50%',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                margin: '0 auto 12px',
                fontSize: 36,
              }}
            >
              💌
            </div>
            <p style={{ fontSize: 32, fontWeight: 800, color: '#fff' }}>
              {result.partnerNumber}번
            </p>
          </div>

          <Button
            variant="outline"
            onClick={() => navigate('/')}
          >
            홈으로 돌아가기
          </Button>
        </>
      ) : (
        <>
          {/* Not matched */}
          <div style={{ fontSize: 72, marginBottom: 20, opacity: 0.6 }}>💔</div>
          <h1
            style={{
              fontSize: 24,
              fontWeight: 800,
              color: 'var(--text)',
              letterSpacing: '-0.5px',
              marginBottom: 8,
            }}
          >
            아쉽지만...
          </h1>
          <p
            style={{
              fontSize: 16,
              color: 'var(--text-light)',
              marginBottom: 40,
              lineHeight: 1.6,
            }}
          >
            이번엔 매칭이 되지 않았습니다.
            <br />
            다음 소개팅에서 꼭 인연을 만나보세요!
          </p>

          <div
            style={{
              background: '#fff',
              borderRadius: 'var(--radius)',
              padding: '20px',
              boxShadow: 'var(--shadow)',
              marginBottom: 24,
              width: '100%',
            }}
          >
            <p style={{ fontSize: 14, color: 'var(--text-light)', lineHeight: 1.7 }}>
              💡 소개팅 클럽에 등록하시면 새로운 이벤트 소식을 가장 먼저 받아보실 수 있어요.
            </p>
          </div>

          <Button fullWidth onClick={() => navigate('/')}>
            다음 소개팅 찾아보기
          </Button>
        </>
      )}
    </div>
  )
}

export default MatchResultPage
