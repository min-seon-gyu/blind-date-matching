import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
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

import ProtectedRoute from '@/components/common/ProtectedRoute'
import ParticipantLayout from '@/components/layout/ParticipantLayout'
import DashboardLayout from '@/components/layout/DashboardLayout'

// Participant pages
import LoginPage from '@/pages/participant/LoginPage'
import KakaoCallbackPage from '@/pages/participant/KakaoCallbackPage'
import ProfileSetupPage from '@/pages/participant/ProfileSetupPage'
import CafeMainPage from '@/pages/participant/CafeMainPage'
import EventDetailPage from '@/pages/participant/EventDetailPage'
import ChoicePage from '@/pages/participant/ChoicePage'
import MatchResultPage from '@/pages/participant/MatchResultPage'
import MyPage from '@/pages/participant/MyPage'
import ProfileEditPage from '@/pages/participant/ProfileEditPage'
import MyApplicationsPage from '@/pages/participant/MyApplicationsPage'
import NotificationsPage from '@/pages/participant/NotificationsPage'

// Organizer pages
import OrganizerLoginPage from '@/pages/organizer/LoginPage'
import OrganizerDashboardPage from '@/pages/organizer/DashboardPage'
import OrganizerEventsPage from '@/pages/organizer/EventsPage'
import OrganizerEventCreatePage from '@/pages/organizer/EventCreatePage'
import OrganizerEventDetailPage from '@/pages/organizer/EventDetailPage'
import OrganizerPartnershipsPage from '@/pages/organizer/PartnershipsPage'
import OrganizerCommissionsPage from '@/pages/organizer/CommissionsPage'
import OrganizerNotificationsPage from '@/pages/organizer/NotificationsPage'

// Cafe Owner pages
import CafeOwnerLoginPage from '@/pages/cafe-owner/LoginPage'
import CafeOwnerDashboardPage from '@/pages/cafe-owner/DashboardPage'
import MyCafePage from '@/pages/cafe-owner/MyCafePage'
import CafeOwnerPartnershipsPage from '@/pages/cafe-owner/PartnershipsPage'
import CafeOwnerEventsPage from '@/pages/cafe-owner/EventsPage'
import CafeOwnerCommissionsPage from '@/pages/cafe-owner/CommissionsPage'
import CafeOwnerNotificationsPage from '@/pages/cafe-owner/NotificationsPage'

// Admin pages
import AdminLoginPage from '@/pages/admin/LoginPage'
import AdminDashboardPage from '@/pages/admin/DashboardPage'
import AdminCafesPage from '@/pages/admin/CafesPage'
import AdminOrganizersPage from '@/pages/admin/OrganizersPage'
import AdminCommissionsPage from '@/pages/admin/CommissionsPage'
import AdminPartnershipsPage from '@/pages/admin/PartnershipsPage'

// Marketplace pages
import MarketplacePage from '@/pages/marketplace/MarketplacePage'
import CreatePostPage from '@/pages/marketplace/CreatePostPage'
import PostDetailPage from '@/pages/marketplace/PostDetailPage'
import EditPostPage from '@/pages/marketplace/EditPostPage'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 30000,
    },
  },
})

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

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          {/* Public */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/auth/kakao/callback" element={<KakaoCallbackPage />} />
          <Route path="/organizer/login" element={<OrganizerLoginPage />} />
          <Route path="/cafe-owner/login" element={<CafeOwnerLoginPage />} />
          <Route path="/admin/login" element={<AdminLoginPage />} />

          {/* Participant (protected) */}
          <Route element={<ProtectedRoute userType="PARTICIPANT" />}>
            <Route path="/profile/setup" element={<ProfileSetupPage />} />
            <Route element={<ParticipantLayout />}>
              <Route path="/cafes/:slug" element={<CafeMainPage />} />
              <Route path="/cafes/:slug/events/:eventId" element={<EventDetailPage />} />
              <Route path="/cafes/:slug/events/:eventId/choose" element={<ChoicePage />} />
              <Route path="/cafes/:slug/events/:eventId/result" element={<MatchResultPage />} />
              <Route path="/me" element={<MyPage />} />
              <Route path="/me/profile" element={<ProfileEditPage />} />
              <Route path="/me/applications" element={<MyApplicationsPage />} />
              <Route path="/me/notifications" element={<NotificationsPage />} />
            </Route>
          </Route>

          {/* Organizer (protected) */}
          <Route element={<ProtectedRoute userType="ORGANIZER" />}>
            <Route element={<DashboardLayout role="organizer" menuItems={organizerMenuItems} />}>
              <Route path="/organizer/dashboard" element={<OrganizerDashboardPage />} />
              <Route path="/organizer/events" element={<OrganizerEventsPage />} />
              <Route path="/organizer/events/new" element={<OrganizerEventCreatePage />} />
              <Route path="/organizer/events/:id" element={<OrganizerEventDetailPage />} />
              <Route path="/organizer/partnerships" element={<OrganizerPartnershipsPage />} />
              <Route path="/organizer/commissions" element={<OrganizerCommissionsPage />} />
              <Route path="/organizer/notifications" element={<OrganizerNotificationsPage />} />
            </Route>
          </Route>

          {/* Cafe Owner (protected) */}
          <Route element={<ProtectedRoute userType="CAFE_OWNER" />}>
            <Route element={<DashboardLayout role="cafe-owner" menuItems={cafeOwnerMenuItems} />}>
              <Route path="/cafe-owner/dashboard" element={<CafeOwnerDashboardPage />} />
              <Route path="/cafe-owner/my-cafe" element={<MyCafePage />} />
              <Route path="/cafe-owner/events" element={<CafeOwnerEventsPage />} />
              <Route path="/cafe-owner/partnerships" element={<CafeOwnerPartnershipsPage />} />
              <Route path="/cafe-owner/commissions" element={<CafeOwnerCommissionsPage />} />
              <Route path="/cafe-owner/notifications" element={<CafeOwnerNotificationsPage />} />
            </Route>
          </Route>

          {/* Admin (protected) */}
          <Route element={<ProtectedRoute userType="PLATFORM_ADMIN" />}>
            <Route element={<DashboardLayout role="admin" menuItems={adminMenuItems} />}>
              <Route path="/admin/dashboard" element={<AdminDashboardPage />} />
              <Route path="/admin/cafes" element={<AdminCafesPage />} />
              <Route path="/admin/organizers" element={<AdminOrganizersPage />} />
              <Route path="/admin/commissions" element={<AdminCommissionsPage />} />
              <Route path="/admin/partnerships" element={<AdminPartnershipsPage />} />
            </Route>
          </Route>

          {/* Marketplace (accessible by Organizer and Cafe Owner) */}
          <Route element={<ProtectedRoute userType={['ORGANIZER', 'CAFE_OWNER']} />}>
            <Route element={<DashboardLayout role="organizer" menuItems={organizerMenuItems} />}>
              <Route path="/marketplace" element={<MarketplacePage />} />
              <Route path="/marketplace/new" element={<CreatePostPage />} />
              <Route path="/marketplace/:id" element={<PostDetailPage />} />
              <Route path="/marketplace/:id/edit" element={<EditPostPage />} />
            </Route>
          </Route>
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  )
}

export default App
