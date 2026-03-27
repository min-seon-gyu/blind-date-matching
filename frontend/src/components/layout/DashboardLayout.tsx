import { useState } from 'react'
import { Outlet, NavLink, useNavigate } from 'react-router-dom'
import { Menu, LogOut } from 'lucide-react'
import { Button } from '@/components/ui/button'
import {
  Sheet,
  SheetTrigger,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from '@/components/ui/sheet'
import { useAuthStore } from '@/stores/authStore'

interface MenuItem {
  label: string
  path: string
  icon: React.ReactNode
}

interface DashboardLayoutProps {
  role: 'organizer' | 'cafe-owner' | 'admin'
  menuItems: MenuItem[]
}

const roleLabels: Record<DashboardLayoutProps['role'], string> = {
  organizer: '주최자',
  'cafe-owner': '카페 사장님',
  admin: '관리자',
}

function SidebarNav({
  menuItems,
  role,
  onLogout,
  onItemClick,
}: {
  menuItems: MenuItem[]
  role: DashboardLayoutProps['role']
  onLogout: () => void
  onItemClick?: () => void
}) {
  return (
    <div className="flex flex-col h-full">
      {/* Logo / Role */}
      <div className="p-6 border-b border-gray-100">
        <h1 className="text-lg font-bold text-[#FF6B6B]">소개팅 매칭</h1>
        <p className="text-sm text-gray-500 mt-1">{roleLabels[role]}</p>
      </div>

      {/* Navigation */}
      <nav className="flex-1 p-4 space-y-1">
        {menuItems.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            onClick={onItemClick}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                isActive
                  ? 'bg-[#FFF5F5] text-[#FF6B6B]'
                  : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
              }`
            }
          >
            {item.icon}
            <span>{item.label}</span>
          </NavLink>
        ))}
      </nav>

      {/* Logout */}
      <div className="p-4 border-t border-gray-100">
        <button
          onClick={onLogout}
          className="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium text-gray-600 hover:bg-gray-50 hover:text-gray-900 transition-colors w-full"
        >
          <LogOut className="w-5 h-5" />
          <span>로그아웃</span>
        </button>
      </div>
    </div>
  )
}

export default function DashboardLayout({ role, menuItems }: DashboardLayoutProps) {
  const [sheetOpen, setSheetOpen] = useState(false)
  const navigate = useNavigate()
  const logout = useAuthStore((s) => s.logout)

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Desktop Sidebar */}
      <aside className="hidden lg:fixed lg:inset-y-0 lg:left-0 lg:flex lg:w-64 lg:flex-col bg-white border-r border-gray-200">
        <SidebarNav menuItems={menuItems} role={role} onLogout={handleLogout} />
      </aside>

      {/* Mobile Header */}
      <header className="lg:hidden sticky top-0 z-40 flex items-center justify-between h-14 px-4 bg-white border-b border-gray-200">
        <h1 className="text-base font-bold text-[#FF6B6B]">소개팅 매칭</h1>
        <Sheet open={sheetOpen} onOpenChange={setSheetOpen}>
          <SheetTrigger
            render={
              <Button variant="ghost" size="icon">
                <Menu className="w-5 h-5" />
                <span className="sr-only">메뉴 열기</span>
              </Button>
            }
          />
          <SheetContent side="left" showCloseButton={false}>
            <SheetHeader className="sr-only">
              <SheetTitle>내비게이션 메뉴</SheetTitle>
            </SheetHeader>
            <SidebarNav
              menuItems={menuItems}
              role={role}
              onLogout={() => {
                setSheetOpen(false)
                handleLogout()
              }}
              onItemClick={() => setSheetOpen(false)}
            />
          </SheetContent>
        </Sheet>
      </header>

      {/* Main Content */}
      <main className="lg:pl-64">
        <div className="p-4 lg:p-8">
          <Outlet />
        </div>
      </main>
    </div>
  )
}
