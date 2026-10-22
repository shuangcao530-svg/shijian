import request from './request'

export const messageApi = {
  send(toUserId, content) { return request.post('/message', { toUserId, content }) },
  getConversations() { return request.get('/message/conversations') },
  getMessages(friendId, page = 1, size = 50) { return request.get(`/message/with/${friendId}`, { params: { page, size } }) },
  markRead(friendId) { return request.put(`/message/read/${friendId}`) },
  getUnreadCount() { return request.get('/message/unread') }
}
