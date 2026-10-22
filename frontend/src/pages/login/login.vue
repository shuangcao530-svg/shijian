<template>
  <view class="login-page">
    <view class="login-card">
      <view class="logo-area">
        <text class="logo-icon">🌿</text>
        <text class="logo-title">校园日常分享</text>
        <text class="logo-desc">记录校园里的新鲜事</text>
      </view>
      <view class="form-area">
        <input class="input" v-model="account" placeholder="用户名 / 手机号" />
        <input class="input" v-model="password" type="password" placeholder="密码" />
        <button class="btn-primary" :loading="loading" @tap="onLogin">登录</button>
      </view>
      <view class="footer-link" @tap="goRegister">还没有账号？<text class="link">立即注册</text></view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { useUserStore } from '../../store/user'
import { userApi } from '../../api/user'

const userStore = useUserStore()
const account = ref('')
const password = ref('')
const loading = ref(false)

async function onLogin() {
  if (!account.value) return uni.showToast({ title: '请输入账号', icon: 'none' })
  if (!password.value) return uni.showToast({ title: '请输入密码', icon: 'none' })
  loading.value = true
  try {
    const res = await userApi.login({ account: account.value, password: password.value })
    userStore.setAuth(res.data.token, res.data.user)
    uni.showToast({ title: '登录成功' })
    uni.reLaunch({ url: '/pages/index/index' })
  } catch (e) {} finally { loading.value = false }
}

function goRegister() { uni.navigateTo({ url: '/pages/register/register' }) }
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg, #DDF4E4 0%, #A8E0BF 45%, #EEF8F0 100%);
  padding: 48rpx;
}
.login-card {
  width: 100%;
  background: rgba(255,255,255,0.94);
  border-radius: 28rpx;
  padding: 64rpx 48rpx;
  border: 1rpx solid rgba(79,174,132,0.14);
  box-shadow: 0 24rpx 58rpx rgba(39,95,70,0.14);
}
.logo-area { text-align: center; margin-bottom: 48rpx; }
.logo-icon { font-size: 80rpx; }
.logo-title { display: block; font-size: 44rpx; font-weight: bold; color: #173B2F; margin-top: 16rpx; }
.logo-desc { display: block; font-size: 26rpx; color: #6F897D; margin-top: 8rpx; }
.form-area { display: flex; flex-direction: column; gap: 24rpx; }
.input {
  height: 88rpx;
  background: #F4FBF6;
  border: 1rpx solid #D8ECDF;
  border-radius: 16rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
}
.btn-primary {
  height: 88rpx;
  background: linear-gradient(135deg, #2F8F68, #66C99A);
  color: #fff;
  border-radius: 16rpx;
  font-size: 32rpx;
  font-weight: bold;
  border: none;
  margin-top: 16rpx;
  box-shadow: 0 14rpx 28rpx rgba(47,143,104,0.24);
}
.footer-link { text-align: center; font-size: 26rpx; color: #6F897D; margin-top: 32rpx; }
.link { color: #2F8F68; font-weight: 600; }
</style>
