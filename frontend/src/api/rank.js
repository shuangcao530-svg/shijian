import request from './request'

export const rankApi = {
  getHot(limit = 10) { return request.get('/rank/hot', { params: { limit } }) },
  getWeekly(limit = 10) { return request.get('/rank/weekly', { params: { limit } }) },
  getMonthly(limit = 10) { return request.get('/rank/monthly', { params: { limit } }) },
  getByCategory(categoryId, limit = 10) { return request.get(`/rank/category/${categoryId}`, { params: { limit } }) }
}
