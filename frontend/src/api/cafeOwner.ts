import client from './client'

export const cafeOwnerApi = {
  login: (email: string, password: string) =>
    client.post('/cafe-owner/auth/login', { email, password }),
  getDashboard: () => client.get('/cafe-owner/dashboard'),
  getMyCafe: () => client.get('/cafe-owner/my-cafe'),
  updateMyCafe: (data: any) => client.put('/cafe-owner/my-cafe', data),
  getEvents: () => client.get('/cafe-owner/events'),
  getCommissions: () => client.get('/cafe-owner/commissions'),
  getPartnerships: () => client.get('/cafe-owner/partnerships'),
  acceptPartnership: (id: number) =>
    client.put(`/cafe-owner/partnerships/${id}/accept`),
  rejectPartnership: (id: number) =>
    client.put(`/cafe-owner/partnerships/${id}/reject`),
  terminatePartnership: (id: number) =>
    client.put(`/cafe-owner/partnerships/${id}/terminate`),
  getNotifications: () => client.get('/cafe-owner/notifications'),
  markNotificationAsRead: (id: number) =>
    client.put(`/cafe-owner/notifications/${id}/read`),
}
