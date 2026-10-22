import request from './request'

export const notificationApi = {
  list(page = 1, size = 20) {
    return request.get('/notification', { params: { page, size } })
  },
  getUnreadCount() {
    return request.get('/notification/unread-count')
  },
  markRead(id) {
    return request.put(`/notification/${id}/read`)
  },
  markAllRead() {
    return request.put('/notification/read-all')
  },
  delete(id) {
    return request.delete(`/notification/${id}`)
  }
}
