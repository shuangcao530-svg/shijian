import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(config => {
  const token = uni.getStorageSync('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      uni.showToast({ title: res.message || '请求失败', icon: 'none' })
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  error => {
    if (error.response && error.response.status === 401) {
      uni.removeStorageSync('token')
      uni.removeStorageSync('user')
      uni.reLaunch({ url: '/pages/login/login' })
      uni.showToast({ title: '登录已过期', icon: 'none' })
    } else {
      uni.showToast({ title: error.message || '网络错误', icon: 'none' })
    }
    return Promise.reject(error)
  }
)

export default request
