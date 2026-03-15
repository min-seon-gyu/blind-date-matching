import React from 'react'

interface ButtonProps {
  children: React.ReactNode
  onClick?: () => void
  variant?: 'primary' | 'admin' | 'outline'
  disabled?: boolean
  fullWidth?: boolean
  type?: 'button' | 'submit' | 'reset'
}

const Button: React.FC<ButtonProps> = ({
  children,
  onClick,
  variant = 'primary',
  disabled = false,
  fullWidth = false,
  type = 'button',
}) => {
  const baseStyle: React.CSSProperties = {
    padding: '12px 24px',
    borderRadius: '12px',
    border: 'none',
    fontSize: '15px',
    fontWeight: 600,
    cursor: disabled ? 'not-allowed' : 'pointer',
    opacity: disabled ? 0.6 : 1,
    width: fullWidth ? '100%' : 'auto',
    transition: 'opacity 0.2s, transform 0.1s',
    fontFamily: 'inherit',
    letterSpacing: '-0.3px',
  }

  const variantStyles: Record<string, React.CSSProperties> = {
    primary: {
      background: 'var(--gradient)',
      color: '#fff',
    },
    admin: {
      background: 'var(--admin-gradient)',
      color: '#fff',
    },
    outline: {
      background: 'transparent',
      color: 'var(--primary)',
      border: '2px solid var(--primary)',
    },
  }

  return (
    <button
      type={type}
      onClick={onClick}
      disabled={disabled}
      style={{ ...baseStyle, ...variantStyles[variant] }}
      onMouseDown={(e) => {
        if (!disabled) (e.currentTarget.style.transform = 'scale(0.97)')
      }}
      onMouseUp={(e) => {
        (e.currentTarget.style.transform = 'scale(1)')
      }}
      onMouseLeave={(e) => {
        (e.currentTarget.style.transform = 'scale(1)')
      }}
    >
      {children}
    </button>
  )
}

export default Button
