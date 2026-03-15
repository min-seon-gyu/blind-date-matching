import { useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { kakaoLogin } from '../api/auth'
import { useAuthStore } from '../stores/authStore'

const KakaoCallbackPage = () => {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const setAuth = useAuthStore((s) => s.setAuth)

  useEffect(() => {
    const code = searchParams.get('code')
    if (!code) {
      navigate('/login')
      return
    }

    kakaoLogin(code)
      .then((data) => {
        setAuth(data.accessToken, {
          id: data.member.id,
          role: data.member.role,
          hasProfile: data.member.hasProfile,
        })
        if (!data.member.hasProfile) {
          navigate('/profile/setup')
        } else {
          navigate('/')
        }
      })
      .catch(() => {
        navigate('/login')
      })
  }, [searchParams, navigate, setAuth])

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'var(--gradient)',
        gap: 16,
      }}
    >
      <div style={{ fontSize: 48 }}>💕</div>
      <p
        style={{
          color: '#fff',
          fontSize: 16,
          fontWeight: 500,
          letterSpacing: '-0.3px',
        }}
      >
        로그인 중입니다...
      </p>
      <div
        style={{
          width: 40,
          height: 40,
          border: '3px solid rgba(255,255,255,0.3)',
          borderTop: '3px solid #fff',
          borderRadius: '50%',
          animation: 'spin 0.8s linear infinite',
        }}
      />
      <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
    </div>
  )
}

export default KakaoCallbackPage
