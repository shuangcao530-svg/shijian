import request from './request'

export const reportApi = {
  submit(data) { return request.post('/report', data) }
}
