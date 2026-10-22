import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { userApi } from '../api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(uni.getStorageSync('token') || '')
  const user = ref(JSON.parse(uni.getStorageSync('user') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  function setAuth(t, u) {
    token.value = t
    user.value = u
    uni.setStorageSync('token', t)
    uni.setStorageSync('user', JSON.stringify(u))
  }

  function logout() {
    token.value = ''
    user.value = null
    uni.removeStorageSync('token')
    uni.removeStorageSync('user')
  }

  async function fetchProfile() {
    try {
      const res = await userApi.getProfile()
      user.value = res.data
      uni.setStorageSync('user', JSON.stringify(res.data))
    } catch (e) {
      logout()
    }
  }

  return { token, user, isLoggedIn, isAdmin, setAuth, logout, fetchProfile }
})
