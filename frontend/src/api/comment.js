import request from './request'

export const commentApi = {
  create(data) { return request.post('/comment', data) },
  delete(id) { return request.delete(`/comment/${id}`) },
  like(id) { return request.post(`/comment/${id}/like`) },
  unlike(id) { return request.delete(`/comment/${id}/like`) },
  getByPostId(postId) { return request.get(`/comment/post/${postId}`) }
}
