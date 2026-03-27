import { Button } from '@/components/ui/button'
import type { LucideIcon } from 'lucide-react'

interface EmptyStateProps {
  icon?: LucideIcon
  emoji?: string
  message: string
  description?: string
  actionLabel?: string
  onAction?: () => void
}

export default function EmptyState({
  icon: Icon,
  emoji,
  message,
  description,
  actionLabel,
  onAction,
}: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center justify-center py-16 px-6 text-center">
      {Icon && (
        <div className="w-16 h-16 rounded-full bg-gray-100 flex items-center justify-center mb-4">
          <Icon className="w-8 h-8 text-gray-400" />
        </div>
      )}
      {emoji && !Icon && (
        <span className="text-5xl mb-4">{emoji}</span>
      )}
      <p className="text-base font-medium text-gray-700">{message}</p>
      {description && (
        <p className="mt-1 text-sm text-gray-400">{description}</p>
      )}
      {actionLabel && onAction && (
        <Button
          onClick={onAction}
          className="mt-4 rounded-xl bg-[#FF6B6B] hover:bg-[#FF5252] text-white"
        >
          {actionLabel}
        </Button>
      )}
    </div>
  )
}
