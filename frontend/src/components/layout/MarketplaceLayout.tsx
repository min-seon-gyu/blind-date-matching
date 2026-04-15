import {
  LayoutDashboard,
  Calendar,
  Handshake,
  Receipt,
  Store,
  Bell,
  Coffee,
  Users,
} from 'lucide-react'
import { useAuthStore } from '@/stores/authStore'
import DashboardLayout from '@/components/layout/DashboardLayout'

const organizerMenuItems = [
  { label: '대시보드', path: '/organizer/dashboard', icon: <LayoutDashboard className="w-5 h-5" /> },
  { label: '이벤트 관리', path: '/organizer/events', icon: <Calendar className="w-5 h-5" /> },
  { label: '제휴 카페', path: '/organizer/partnerships', icon: <Handshake className="w-5 h-5" /> },
  { label: '수수료', path: '/organizer/commissions', icon: <Receipt className="w-5 h-5" /> },
  { label: '마켓플레이스', path: '/marketplace', icon: <Store className="w-5 h-5" /> },
  { label: '알림', path: '/organizer/notifications', icon: <Bell className="w-5 h-5" /> },
]

const cafeOwnerMenuItems = [
  { label: '대시보드', path: '/cafe-owner/dashboard', icon: <LayoutDashboard className="w-5 h-5" /> },
  { label: '내 카페', path: '/cafe-owner/my-cafe', icon: <Coffee className="w-5 h-5" /> },
  { label: '이벤트', path: '/cafe-owner/events', icon: <Calendar className="w-5 h-5" /> },
  { label: '제휴 관리', path: '/cafe-owner/partnerships', icon: <Handshake className="w-5 h-5" /> },
  { label: '수수료', path: '/cafe-owner/commissions', icon: <Receipt className="w-5 h-5" /> },
  { label: '마켓플레이스', path: '/marketplace', icon: <Store className="w-5 h-5" /> },
  { label: '알림', path: '/cafe-owner/notifications', icon: <Bell className="w-5 h-5" /> },
]

const adminMenuItems = [
  { label: '대시보드', path: '/admin/dashboard', icon: <LayoutDashboard className="w-5 h-5" /> },
  { label: '카페 관리', path: '/admin/cafes', icon: <Coffee className="w-5 h-5" /> },
  { label: '주관자 관리', path: '/admin/organizers', icon: <Users className="w-5 h-5" /> },
  { label: '수수료', path: '/admin/commissions', icon: <Receipt className="w-5 h-5" /> },
  { label: '제휴 현황', path: '/admin/partnerships', icon: <Handshake className="w-5 h-5" /> },
]

export { organizerMenuItems, cafeOwnerMenuItems, adminMenuItems }

export default function MarketplaceLayout() {
  const { user } = useAuthStore()

  if (user?.userType === 'CAFE_OWNER') {
    return <DashboardLayout role="cafe-owner" menuItems={cafeOwnerMenuItems} />
  }

  // Default to organizer layout
  return <DashboardLayout role="organizer" menuItems={organizerMenuItems} />
}
