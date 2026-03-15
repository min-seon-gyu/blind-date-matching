interface CapacityBarProps {
  current: number
  total: number
  gender: 'MALE' | 'FEMALE'
  label?: string
}

const CapacityBar: React.FC<CapacityBarProps> = ({ current, total, gender, label }) => {
  const pct = total > 0 ? Math.min((current / total) * 100, 100) : 0
  const isMale = gender === 'MALE'

  return (
    <div style={{ marginBottom: 10 }}>
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: 6,
        }}
      >
        <span style={{ fontSize: 13, fontWeight: 600, color: isMale ? '#4a90d9' : '#ff6b8a' }}>
          {isMale ? '👨 남성' : '👩 여성'} {label && `(${label})`}
        </span>
        <span style={{ fontSize: 13, color: 'var(--text-light)' }}>
          {current} / {total}명
        </span>
      </div>
      <div
        style={{
          height: 8,
          background: '#f0f0f0',
          borderRadius: 4,
          overflow: 'hidden',
        }}
      >
        <div
          style={{
            height: '100%',
            width: `${pct}%`,
            background: isMale
              ? 'linear-gradient(90deg, #4a90d9, #74b0e8)'
              : 'linear-gradient(90deg, #ff6b8a, #ff8e53)',
            borderRadius: 4,
            transition: 'width 0.3s',
          }}
        />
      </div>
      <div style={{ fontSize: 11, color: 'var(--text-light)', marginTop: 3 }}>
        잔여 {Math.max(total - current, 0)}자리
      </div>
    </div>
  )
}

export default CapacityBar
