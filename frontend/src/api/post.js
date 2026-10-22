import request from './request'

export const postApi = {
  create(data) { return request.post('/post', data) },
  getById(id) { return request.get(`/post/${id}`) },
  update(id, data) { return request.put(`/post/${id}`, data) },
  delete(id) { return request.delete(`/post/${id}`) },
  page(params) { return request.get('/post', { params }) },
  myPosts(page = 1, size = 10) { return request.get('/post/my', { params: { page, size } }) },
  userPosts(userId, page = 1, size = 10) {
    return request.get(`/post/user/${userId}`, { params: { page, size } })
  },
  friendPosts(page = 1, size = 10) {
    return request.get('/post/friends', { params: { page, size } })
  }
}
