import request from './request'

export const friendApi = {
  sendRequest(toUserId, message) { return request.post('/friend/request', { toUserId, message }) },
  handleRequest(id, action) { return request.put(`/friend/request/${id}`, null, { params: { action } }) },
  getRequests() { return request.get('/friend/requests') },
  deleteFriend(friendId) { return request.delete(`/friend/${friendId}`) },
  getFriends(page = 1, size = 20) { return request.get('/friend', { params: { page, size } }) },
  addToBlacklist(userId) { return request.post(`/friend/blacklist/${userId}`) },
  removeFromBlacklist(userId) { return request.delete(`/friend/blacklist/${userId}`) },
  getBlacklist(page = 1, size = 20) { return request.get('/friend/blacklist', { params: { page, size } }) }
}
