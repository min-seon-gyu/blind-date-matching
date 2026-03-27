import { useEffect, useRef } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { kakaoLogin } from '@/api/auth'
import { useAuthStore } from '@/stores/authStore'
import { Loader2 } from 'lucide-react'

export default function KakaoCallbackPage() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const setAuth = useAuthStore((s) => s.setAuth)
  const calledRef = useRef(false)

  useEffect(() => {
    if (calledRef.current) return
    calledRef.current = true

    const code = searchParams.get('code')
    if (!code) {
      navigate('/login', { replace: true })
      return
    }

    kakaoLogin(code)
      .then((data) => {
        setAuth(data.accessToken, data.user)
        if (data.user.hasProfile === false) {
          navigate('/profile/setup', { replace: true })
        } else {
          navigate('/me', { replace: true })
        }
      })
      .catch(() => {
        navigate('/login', { replace: true })
      })
  }, [searchParams, navigate, setAuth])

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-[#FFF5F5] gap-4">
      <Loader2 className="w-10 h-10 text-[#FF6B6B] animate-spin" />
      <p className="text-gray-500 text-sm">로그인 처리 중...</p>
    </div>
  )
}
