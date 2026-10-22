import request from './request'

export const favoriteApi = {
  add(postId) { return request.post(`/favorite/${postId}`) },
  remove(postId) { return request.delete(`/favorite/${postId}`) },
  list(page = 1, size = 10) { return request.get('/favorite', { params: { page, size } }) }
}
