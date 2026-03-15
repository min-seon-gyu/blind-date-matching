import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'

import ProtectedRoute from './components/auth/ProtectedRoute'
import AppLayout from './components/layout/AppLayout'

import LoginPage from './pages/LoginPage'
import KakaoCallbackPage from './pages/KakaoCallbackPage'
import ProfileSetupPage from './pages/ProfileSetupPage'
import CalendarPage from './pages/CalendarPage'
import EventDetailPage from './pages/EventDetailPage'
import ChoicePage from './pages/ChoicePage'
import MatchResultPage from './pages/MatchResultPage'
import MyPage from './pages/MyPage'
import MyApplicationsPage from './pages/MyApplicationsPage'
import ProfileEditPage from './pages/ProfileEditPage'
import BarStatusPage from './pages/BarStatusPage'
import BarReservePage from './pages/BarReservePage'
import NotificationsPage from './pages/NotificationsPage'
import AdminEventsPage from './pages/admin/AdminEventsPage'
import AdminApplicationsPage from './pages/admin/AdminApplicationsPage'
import AdminMembersPage from './pages/admin/AdminMembersPage'
import AdminBarPage from './pages/admin/AdminBarPage'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 1000 * 30,
    },
  },
})

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/auth/kakao/callback" element={<KakaoCallbackPage />} />
          <Route element={<ProtectedRoute />}>
            <Route path="/profile/setup" element={<ProfileSetupPage />} />
            <Route element={<AppLayout />}>
              <Route path="/" element={<CalendarPage />} />
              <Route path="/events/:id" element={<EventDetailPage />} />
              <Route path="/events/:id/choose" element={<ChoicePage />} />
              <Route path="/events/:id/result" element={<MatchResultPage />} />
              <Route path="/mypage" element={<MyPage />} />
              <Route path="/mypage/applications" element={<MyApplicationsPage />} />
              <Route path="/mypage/profile" element={<ProfileEditPage />} />
              <Route path="/bar" element={<BarStatusPage />} />
              <Route path="/bar/reserve" element={<BarReservePage />} />
              <Route path="/notifications" element={<NotificationsPage />} />
              <Route path="/admin/events" element={<AdminEventsPage />} />
              <Route path="/admin/applications" element={<AdminApplicationsPage />} />
              <Route path="/admin/members" element={<AdminMembersPage />} />
              <Route path="/admin/bar" element={<AdminBarPage />} />
            </Route>
          </Route>
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  )
}

export default App
