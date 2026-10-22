import request from './request'

export const moderationApi = {
  checkContent(text) {
    return request.post('/admin/moderation/check', { text })
  },
  getSensitiveWords() {
    return request.get('/admin/moderation/sensitive-words')
  },
  addSensitiveWord(word, category, severity) {
    return request.post('/admin/moderation/sensitive-words', { word, category, severity })
  },
  removeSensitiveWord(id) {
    return request.delete(`/admin/moderation/sensitive-words/${id}`)
  }
}
