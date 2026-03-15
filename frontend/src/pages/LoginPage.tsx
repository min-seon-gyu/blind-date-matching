const KAKAO_CLIENT_ID = import.meta.env.VITE_KAKAO_CLIENT_ID || 'YOUR_KAKAO_CLIENT_ID'
const REDIRECT_URI = import.meta.env.VITE_REDIRECT_URI || `${window.location.origin}/auth/kakao/callback`

const LoginPage = () => {
  const handleKakaoLogin = () => {
    const url = `https://kauth.kakao.com/oauth/authorize?client_id=${KAKAO_CLIENT_ID}&redirect_uri=${encodeURIComponent(REDIRECT_URI)}&response_type=code`
    window.location.href = url
  }

  return (
    <div
      style={{
        minHeight: '100vh',
        background: 'var(--gradient)',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '40px 24px',
      }}
    >
      {/* Logo area */}
      <div style={{ textAlign: 'center', marginBottom: 48 }}>
        <div style={{ fontSize: 72, marginBottom: 16 }}>💕</div>
        <h1
          style={{
            fontSize: 36,
            fontWeight: 800,
            color: '#fff',
            letterSpacing: '-1px',
            marginBottom: 8,
          }}
        >
          소개팅
        </h1>
        <p
          style={{
            fontSize: 16,
            color: 'rgba(255,255,255,0.85)',
            fontWeight: 300,
            letterSpacing: '-0.3px',
          }}
        >
          당신의 특별한 인연을 찾아보세요
        </p>
      </div>

      {/* Card */}
      <div
        style={{
          background: '#fff',
          borderRadius: 24,
          padding: '36px 24px',
          width: '100%',
          maxWidth: 360,
          boxShadow: '0 20px 60px rgba(0,0,0,0.15)',
        }}
      >
        <div style={{ textAlign: 'center', marginBottom: 28 }}>
          <p style={{ fontSize: 14, color: 'var(--text-light)', letterSpacing: '-0.2px' }}>
            소셜 계정으로 간편하게 시작하세요
          </p>
        </div>

        {/* Kakao button */}
        <button
          onClick={handleKakaoLogin}
          style={{
            width: '100%',
            padding: '14px',
            background: '#FEE500',
            border: 'none',
            borderRadius: 12,
            fontSize: 16,
            fontWeight: 700,
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 8,
            color: '#3C1E1E',
            letterSpacing: '-0.3px',
            fontFamily: 'inherit',
          }}
        >
          <span style={{ fontSize: 20 }}>💬</span>
          카카오로 시작하기
        </button>

        <div
          style={{
            marginTop: 20,
            padding: '14px',
            background: '#fafafa',
            borderRadius: 10,
            fontSize: 12,
            color: 'var(--text-light)',
            textAlign: 'center',
            lineHeight: 1.6,
          }}
        >
          로그인 시 서비스 이용약관 및 개인정보 처리방침에 동의하게 됩니다.
        </div>
      </div>

      {/* Decorative circles */}
      <div
        style={{
          position: 'fixed',
          top: -80,
          right: -80,
          width: 250,
          height: 250,
          background: 'rgba(255,255,255,0.1)',
          borderRadius: '50%',
          pointerEvents: 'none',
        }}
      />
      <div
        style={{
          position: 'fixed',
          bottom: -100,
          left: -100,
          width: 300,
          height: 300,
          background: 'rgba(255,255,255,0.08)',
          borderRadius: '50%',
          pointerEvents: 'none',
        }}
      />
    </div>
  )
}

export default LoginPage
