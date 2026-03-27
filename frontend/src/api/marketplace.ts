import client from './client'

export const marketplaceApi = {
  getPosts: (params?: { type?: string; region?: string }) =>
    client.get('/marketplace/posts', { params }),
  createPost: (data: any) => client.post('/marketplace/posts', data),
  getPost: (id: number) => client.get(`/marketplace/posts/${id}`),
  updatePost: (id: number, data: any) =>
    client.put(`/marketplace/posts/${id}`, data),
  deletePost: (id: number) => client.delete(`/marketplace/posts/${id}`),
  requestPartnership: (id: number, message?: string) =>
    client.post(`/marketplace/posts/${id}/request-partnership`, { message }),
  getMyPosts: () => client.get('/marketplace/posts/my'),
}
