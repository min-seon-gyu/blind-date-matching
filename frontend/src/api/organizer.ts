import client from './client'

export const organizerApi = {
  login: (email: string, password: string) =>
    client.post('/organizer/auth/login', { email, password }),
  getDashboard: () => client.get('/organizer/dashboard'),
  getEvents: () => client.get('/organizer/events'),
  createEvent: (data: any) => client.post('/organizer/events', data),
  updateEvent: (id: number, data: any) =>
    client.put(`/organizer/events/${id}`, data),
  deleteEvent: (id: number) => client.delete(`/organizer/events/${id}`),
  closeEvent: (id: number) => client.put(`/organizer/events/${id}/close`),
  getApplications: (eventId: number) =>
    client.get(`/organizer/events/${eventId}/applications`),
  approveApplication: (id: number) =>
    client.put(`/organizer/applications/${id}/approve`),
  rejectApplication: (id: number, reason?: string) =>
    client.put(`/organizer/applications/${id}/reject`, { reason }),
  getCommissions: () => client.get('/organizer/commissions'),
  getPartnerships: () => client.get('/organizer/partnerships'),
  getNotifications: () => client.get('/organizer/notifications'),
  markNotificationAsRead: (id: number) =>
    client.put(`/organizer/notifications/${id}/read`),
}
