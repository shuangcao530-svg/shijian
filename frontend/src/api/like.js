import request from './request'

export const likeApi = {
  likePost(postId) { return request.post(`/like/post/${postId}`) },
  unlikePost(postId) { return request.delete(`/like/post/${postId}`) }
}
