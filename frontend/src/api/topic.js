import request from './request'

export const topicApi = {
  listAll() {
    return request.get('/topic')
  },
  listHot(limit = 6) {
    return request.get('/topic/hot', { params: { limit } })
  },
  follow(topicId) {
    return request.post(`/topic/${topicId}/follow`)
  },
  unfollow(topicId) {
    return request.delete(`/topic/${topicId}/follow`)
  },
  myFollows() {
    return request.get('/topic/my-follows')
  },
  getTopicPosts(topicId, params = {}) {
    return request.get(`/topic/${topicId}/posts`, { params })
  },
  checkFollow(topicId) {
    return request.get(`/topic/${topicId}/check-follow`)
  }
}
