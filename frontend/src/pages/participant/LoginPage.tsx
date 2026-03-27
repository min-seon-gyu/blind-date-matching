import { useAuthStore } from '@/stores/authStore'
import { Navigate } from 'react-router-dom'

const KAKAO_CLIENT_ID = import.meta.env.VITE_KAKAO_CLIENT_ID || 'KAKAO_CLIENT_ID_PLACEHOLDER'
const REDIRECT_URI = import.meta.env.VITE_KAKAO_REDIRECT_URI || `${window.location.origin}/auth/kakao/callback`

export default function LoginPage() {
  const { accessToken, user } = useAuthStore()

  if (accessToken && user) {
    return <Navigate to="/me" replace />
  }

  const handleKakaoLogin = () => {
    const kakaoAuthUrl = `https://kauth.kakao.com/oauth/authorize?client_id=${KAKAO_CLIENT_ID}&redirect_uri=${encodeURIComponent(REDIRECT_URI)}&response_type=code`
    window.location.href = kakaoAuthUrl
  }

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gradient-to-br from-[#FF6B6B] to-[#FFE66D] px-6">
      <div className="flex flex-col items-center gap-6 text-center">
        {/* Logo / Icon */}
        <div className="w-24 h-24 bg-white/20 backdrop-blur-sm rounded-full flex items-center justify-center">
          <span className="text-5xl">💕</span>
        </div>

        {/* App Name */}
        <div>
          <h1 className="text-3xl font-bold text-white drop-shadow-sm">
            소개팅 매칭
          </h1>
          <p className="mt-2 text-white/90 text-base">
            설레는 만남을 시작해보세요
          </p>
        </div>

        {/* Kakao Login Button */}
        <button
          onClick={handleKakaoLogin}
          className="mt-8 flex items-center justify-center gap-3 w-full max-w-xs h-12 rounded-xl bg-[#FEE500] text-[#191919] font-semibold text-base shadow-lg hover:brightness-95 active:scale-[0.98] transition-all"
        >
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path
              d="M10 2C5.029 2 1 5.164 1 9.054c0 2.467 1.637 4.635 4.102 5.87l-1.04 3.844c-.09.332.282.6.572.413l4.56-3.013a11.38 11.38 0 001.806.145c4.971 0 9-3.164 9-7.054S14.971 2 10 2z"
              fill="#191919"
            />
          </svg>
          카카오 로그인
        </button>
      </div>
    </div>
  )
}
