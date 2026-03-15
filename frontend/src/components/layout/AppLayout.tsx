import { Outlet } from 'react-router-dom'
import Header from './Header'
import BottomNav from './BottomNav'

const AppLayout = () => {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <Header />
      <main
        style={{
          flex: 1,
          overflowY: 'auto',
          paddingBottom: 70,
        }}
      >
        <Outlet />
      </main>
      <BottomNav />
    </div>
  )
}

export default AppLayout
