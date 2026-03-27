import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'

import ProtectedRoute from '@/components/common/ProtectedRoute'
import ParticipantLayout from '@/components/layout/ParticipantLayout'

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

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 30000,
    },
  },
})

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          {/* Public */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/auth/kakao/callback" element={<KakaoCallbackPage />} />

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
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  )
}

export default App
