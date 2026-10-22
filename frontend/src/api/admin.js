import request from './request'

export const adminApi = {
  getUsers(keyword, page = 1, size = 10) { return request.get('/admin/users', { params: { keyword, page, size } }) },
  toggleUserStatus(userId) { return request.put(`/admin/users/${userId}/status`) },
  getPendingPosts(page = 1, size = 10) { return request.get('/admin/posts/pending', { params: { page, size } }) },
  auditPost(postId, status) { return request.put(`/admin/posts/${postId}/audit`, null, { params: { status } }) },
  addCategory(data) { return request.post('/admin/category', data) },
  updateCategory(id, data) { return request.put(`/admin/category/${id}`, data) },
  deleteCategory(id) { return request.delete(`/admin/category/${id}`) },
  getCategories() { return request.get('/admin/categories') },
  getReports(page = 1, size = 10) { return request.get('/admin/reports', { params: { page, size } }) },
  handleReport(reportId, result, handleResult) { return request.put(`/admin/reports/${reportId}`, null, { params: { result, handleResult } }) },
  getStats() { return request.get('/admin/stats') }
}
