<template>
  <view class="page">
    <view class="card">
      <text class="title">创建账号</text>
      <view class="form">
        <input class="input" v-model="form.username" placeholder="用户名（3-20位）" />
        <input class="input" v-model="form.password" type="password" placeholder="密码（6-20位）" />
        <input class="input" v-model="form.confirmPassword" type="password" placeholder="确认密码" />
        <input class="input" v-model="form.phone" placeholder="手机号（选填）" type="number" />
        <input class="input" v-model="form.nickname" placeholder="昵称（选填）" />
        <button class="btn-primary" :loading="loading" @tap="onRegister">注册</button>
      </view>
      <view class="footer-link" @tap="goLogin">已有账号？<text class="link">立即登录</text></view>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { userApi } from '../../api/user'

const loading = ref(false)
const form = reactive({ username: '', password: '', confirmPassword: '', phone: '', nickname: '' })

async function onRegister() {
  if (!form.username || form.username.length < 3) return uni.showToast({ title: '用户名至少3位', icon: 'none' })
  if (!form.password || form.password.length < 6) return uni.showToast({ title: '密码至少6位', icon: 'none' })
  if (form.password !== form.confirmPassword) return uni.showToast({ title: '两次密码不一致', icon: 'none' })
  loading.value = true
  try {
    await userApi.register({ username: form.username, password: form.password, phone: form.phone, nickname: form.nickname || form.username })
    uni.showToast({ title: '注册成功，请登录' })
    uni.navigateBack()
  } catch (e) {} finally { loading.value = false }
}

function goLogin() { uni.navigateBack() }
</script>

<style scoped>
.page { min-height: 100vh; background: linear-gradient(160deg, #DDF4E4 0%, #A8E0BF 45%, #EEF8F0 100%); padding: 48rpx; display: flex; align-items: center; justify-content: center; }
.card { width: 100%; background: rgba(255,255,255,0.94); border-radius: 28rpx; padding: 48rpx; border: 1rpx solid rgba(79,174,132,0.14); box-shadow: 0 24rpx 58rpx rgba(39,95,70,0.14); }
.title { font-size: 40rpx; font-weight: bold; color: #173B2F; text-align: center; display: block; margin-bottom: 32rpx; }
.form { display: flex; flex-direction: column; gap: 20rpx; }
.input { height: 84rpx; background: #F4FBF6;
  border: 1rpx solid #D8ECDF; border-radius: 12rpx; padding: 0 24rpx; font-size: 28rpx; }
.btn-primary { height: 88rpx; background: linear-gradient(135deg, #2F8F68, #66C99A); color: #fff; border-radius: 16rpx; font-size: 32rpx; font-weight: bold; border: none; margin-top: 12rpx; box-shadow: 0 14rpx 28rpx rgba(47,143,104,0.24); }
.footer-link { text-align: center; font-size: 26rpx; color: #6F897D; margin-top: 28rpx; }
.link { color: #2F8F68; font-weight: 600; }
</style>
