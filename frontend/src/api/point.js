import request from './request'

export const pointApi = {
  checkin() {
    return request.post('/point/checkin')
  },
  checkinStatus() {
    return request.get('/point/checkin/status')
  },
  myPoints() {
    return request.get('/point/my')
  },
  records(page = 1, size = 20) {
    return request.get('/point/records', { params: { page, size } })
  },
  rank(page = 1, size = 20) {
    return request.get('/point/rank', { params: { page, size } })
  }
}
