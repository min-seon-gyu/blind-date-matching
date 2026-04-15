import client from './client'

export const adminApi = {
  login: (email: string, password: string) =>
    client.post('/admin/auth/login', { email, password }),
  getDashboard: () => client.get('/admin/dashboard'),
  getCafes: () => client.get('/admin/cafes'),
  createCafe: (data: any) => client.post('/admin/cafes', data),
  createCafeOwner: (data: any) => client.post('/admin/cafe-owners', data),
  getOrganizers: () => client.get('/admin/organizers'),
  createOrganizer: (data: any) => client.post('/admin/organizers', data),
  getCommissions: () => client.get('/admin/commissions'),
  invoiceCommission: (id: number) =>
    client.put(`/admin/commissions/${id}/invoice`),
  markCommissionPaid: (id: number) =>
    client.put(`/admin/commissions/${id}/paid`),
  getPartnerships: () => client.get('/admin/partnerships'),
}
