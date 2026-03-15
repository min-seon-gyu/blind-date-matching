import { useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { getMembers } from '../../api/member'
import type { Member } from '../../types'

const AdminMembersPage = () => {
  const navigate = useNavigate()

  const { data: members = [], isLoading } = useQuery({
    queryKey: ['adminMembers'],
    queryFn: getMembers,
  })

  const cardStyle: React.CSSProperties = {
    background: '#fff',
    borderRadius: 12,
    padding: '14px 16px',
    boxShadow: '0 2px 8px rgba(99,102,241,0.1)',
    border: '1px solid #e8e0f0',
    marginBottom: 8,
    display: 'flex',
    alignItems: 'center',
    gap: 12,
  }

  return (
    <div style={{ padding: '20px 16px' }}>
      <div
        style={{
          background: 'var(--admin-gradient)',
          borderRadius: 'var(--radius)',
          padding: '18px',
          marginBottom: 20,
          color: '#fff',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
        }}
      >
        <div>
          <h1 style={{ fontSize: 20, fontWeight: 800 }}>⚙️ 회원 관리</h1>
          <p style={{ fontSize: 12, opacity: 0.8, marginTop: 2 }}>전체 회원 목록</p>
        </div>
        <button
          onClick={() => navigate(-1)}
          style={{
            background: 'rgba(255,255,255,0.2)',
            border: 'none',
            borderRadius: 8,
            padding: '6px 12px',
            color: '#fff',
            cursor: 'pointer',
            fontFamily: 'inherit',
            fontSize: 13,
          }}
        >
          뒤로
        </button>
      </div>

      <div
        style={{
          background: 'linear-gradient(135deg, #ede9fe, #faf5ff)',
          borderRadius: 12,
          padding: '12px 16px',
          marginBottom: 16,
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
        }}
      >
        <span style={{ fontSize: 14, fontWeight: 600, color: '#6366f1' }}>전체 회원</span>
        <span style={{ fontSize: 22, fontWeight: 800, color: '#6366f1' }}>{members.length}명</span>
      </div>

      {isLoading ? (
        <p style={{ textAlign: 'center', color: 'var(--text-light)', padding: 20 }}>불러오는 중...</p>
      ) : members.length === 0 ? (
        <div style={{ ...cardStyle, justifyContent: 'center', padding: 30 }}>
          <p style={{ color: 'var(--text-light)' }}>회원이 없습니다.</p>
        </div>
      ) : (
        members.map((m: Member) => (
          <div key={m.id} style={cardStyle}>
            <div
              style={{
                width: 42,
                height: 42,
                background: m.role === 'ADMIN' ? 'var(--admin-gradient)' : 'var(--gradient)',
                borderRadius: '50%',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: 18,
                flexShrink: 0,
              }}
            >
              {m.role === 'ADMIN' ? '⚙️' : '👤'}
            </div>
            <div style={{ flex: 1, minWidth: 0 }}>
              <p style={{ fontWeight: 700, fontSize: 14, color: 'var(--text)', marginBottom: 2 }}>
                {m.nickname}
              </p>
              <p
                style={{
                  fontSize: 12,
                  color: 'var(--text-light)',
                  overflow: 'hidden',
                  textOverflow: 'ellipsis',
                  whiteSpace: 'nowrap',
                }}
              >
                {m.email}
              </p>
            </div>
            <span
              style={{
                fontSize: 11,
                fontWeight: 700,
                background: m.role === 'ADMIN' ? '#ede9fe' : '#f0f4ff',
                color: m.role === 'ADMIN' ? '#6366f1' : '#3b82f6',
                padding: '3px 8px',
                borderRadius: 10,
              }}
            >
              {m.role}
            </span>
          </div>
        ))
      )}
    </div>
  )
}

export default AdminMembersPage
