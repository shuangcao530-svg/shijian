import request from './request'

export const userApi = {
  register(data) { return request.post('/user/register', data) },
  login(data) { return request.post('/user/login', data) },
  getProfile() { return request.get('/user/profile') },
  getUserById(id) { return request.get(`/user/${id}`) },
  updateProfile(data) { return request.put('/user/profile', data) },
  updatePassword(data) { return request.put('/user/password', data) },
  searchUsers(keyword, page = 1, size = 10) {
    return request.get('/user/search', { params: { keyword, page, size } })
  }
}
