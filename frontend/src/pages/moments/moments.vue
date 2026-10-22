<template>
  <view class="page">
    <view v-if="posts.length === 0 && !loading" class="empty">
      <text class="empty-icon">🌿</text>
      <text class="empty-text">好友还没有发布内容</text>
    </view>

    <view v-for="post in posts" :key="post.id" class="post-card" @tap="goDetail(post.id)">
      <view class="card-accent" :class="'accent-' + levelColor(post.level)"></view>
      <view class="card-body">
        <view class="post-header">
          <image class="avatar" :src="post.userAvatar || '/static/default-avatar.svg'" mode="aspectFill" @tap.stop="goUser(post.userId)" />
          <view class="post-meta" @tap.stop="goUser(post.userId)">
            <text class="post-author">{{ post.nickname }}</text>
            <text class="post-time">{{ post.createTime }}</text>
          </view>
        </view>
        <text class="post-title">{{ post.title }}</text>
        <text class="post-excerpt">{{ post.content?.substring(0, 150) }}{{ post.content?.length > 150 ? '...' : '' }}</text>
        <view class="post-images" v-if="post.images?.length">
          <view class="img-grid" :class="'grid-' + Math.min(post.images.length, 3)">
            <image v-for="(img, i) in post.images.slice(0, 3)" :key="i" :src="img" mode="aspectFill" class="post-img" />
          </view>
        </view>
        <view class="post-footer">
          <text class="footer-tag">{{ post.categoryName }}</text>
          <text v-if="post.location" class="footer-loc">📍 {{ post.location }}</text>
          <view class="post-stats">
            <text class="stat">❤️ {{ post.likeCount }}</text>
            <text class="stat">💬 {{ post.commentCount }}</text>
          </view>
        </view>
      </view>
    </view>

    <view v-if="hasMore" class="load-more" @tap="loadMore">
      <text>{{ loading ? '加载中...' : '— 加载更多 —' }}</text>
    </view>

    <!-- TabBar -->
    <view class="tab-bar">
      <view class="t-item" @tap="goTab(0, '/pages/index/index')"><text class="t-icon">🏠</text><text class="t-text" :class="{ on: tabIdx===0 }">首页</text></view>
      <view class="t-item" @tap="goTab(1, '/pages/moments/moments')"><text class="t-icon">🌿</text><text class="t-text" :class="{ on: tabIdx===1 }">好友圈</text></view>
      <view class="t-item" @tap="goTab(2, '/pages/post/create/create')"><view class="t-plus"><text class="t-plus-text">＋</text></view></view>
      <view class="t-item" @tap="goTab(3, '/pages/messages/messages')">
  <view class="t-icon-wrap">
    <text class="t-icon">💬</text>
    <view v-if="unread > 0" class="t-badge">{{ unread > 99 ? '99+' : unread }}</view>
  </view>
  <text class="t-text" :class="{ on: tabIdx===3 }">消息</text>
</view>
      <view class="t-item" @tap="goTab(4, '/pages/user/profile/profile')"><text class="t-icon">👤</text><text class="t-text" :class="{ on: tabIdx===4 }">我的</text></view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { postApi } from '../../api/post'
import { messageApi } from '../../api/message'

const posts = ref([])
const page = ref(1)
const hasMore = ref(true)
const loading = ref(false)
const tabIdx = ref(1)
const unread = ref(0)

const levelColors = { 1: 'safe', 2: 'warn', 3: 'warn', 4: 'danger', 5: 'danger' }
function levelColor(l) { return levelColors[l] || 'safe' }

function goTab(i, url) {
  if (i === tabIdx.value) return
  const token = uni.getStorageSync('token')
  if ([2,3,4].includes(i) && !token) { uni.showToast({ title: '请先登录', icon: 'none' }); uni.navigateTo({ url: '/pages/login/login' }); return }
  tabIdx.value = i; uni.reLaunch({ url })
}
function goUser(id) { uni.navigateTo({ url: `/pages/user/info/info?id=${id}` }) }
function goDetail(id) { uni.navigateTo({ url: `/pages/post/detail/detail?id=${id}` }) }

async function fetchPosts(reset) {
  if (loading.value) return
  if (reset) { page.value = 1; posts.value = [] }
  loading.value = true
  try {
    const res = await postApi.friendPosts(page.value, 10)
    if (reset) posts.value = res.data.records
    else posts.value = [...posts.value, ...res.data.records]
    hasMore.value = res.data.records.length >= 10
    page.value++
  } finally { loading.value = false }
}
function loadMore() { fetchPosts(false) }

onMounted(async () => {
  fetchPosts(true)
  messageApi.getUnreadCount().then(r => { unread.value = r.data || 0 }).catch(() => {})
})
</script>

<style scoped>
.page { padding: 20rpx 24rpx 140rpx; min-height: 100vh; background: #EEF8F0; }
.empty { text-align: center; padding: 200rpx 0; }
.empty-icon { font-size: 100rpx; display: block; margin-bottom: 20rpx; }
.empty-text { font-size: 28rpx; color: #8DA49A; }

.post-card { background: #fff; margin-bottom: 20rpx; border-radius: 20rpx; overflow: hidden; box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.04); position: relative; }
.card-accent { position: absolute; left: 0; top: 0; bottom: 0; width: 6rpx; }
.accent-safe { background: #52B788; }
.accent-warn { background: #E5A63A; }
.accent-danger { background: #D96666; }
.card-body { padding: 24rpx; }
.post-header { display: flex; align-items: center; gap: 14rpx; }
.avatar { width: 76rpx; height: 76rpx; border-radius: 50%; border: 3rpx solid #F1F2F6; }
.post-meta { flex: 1; }
.post-author { font-weight: 700; font-size: 28rpx; display: block; }
.post-time { font-size: 22rpx; color: #B2BEC3; }
.post-title { font-size: 30rpx; font-weight: 700; margin: 14rpx 0 8rpx; display: block; }
.post-excerpt { font-size: 26rpx; color: #636E72; line-height: 1.6; display: block; }
.post-images { margin-top: 12rpx; }
.img-grid { display: flex; gap: 8rpx; }
.img-grid.grid-1 .post-img { width: 100%; height: 360rpx; }
.img-grid.grid-2 .post-img { width: calc(50% - 4rpx); height: 220rpx; }
.img-grid.grid-3 .post-img { width: calc(33.33% - 6rpx); height: 200rpx; }
.post-img { border-radius: 14rpx; }
.post-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 14rpx; padding-top: 14rpx; border-top: 1rpx solid #F1F2F6; }
.footer-tag { font-size: 22rpx; color: #4FAE84; background: #E7F7EC; padding: 6rpx 18rpx; border-radius: 20rpx; }
.footer-loc { font-size: 20rpx; color: #B2BEC3; margin-left: 8rpx; }
.post-stats { display: flex; gap: 16rpx; }
.stat { font-size: 22rpx; color: #B2BEC3; }
.load-more { text-align: center; padding: 28rpx; font-size: 26rpx; color: #8DA49A; }

.tab-bar { position: fixed; bottom: 0; left: 0; right: 0; height: 100rpx; display: flex; justify-content: space-around; align-items: center; background: #fff; border-top: 1rpx solid #eee; padding-bottom: env(safe-area-inset-bottom); z-index: 999; }
.t-item { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 2rpx; }
.t-icon { font-size: 38rpx; }
.t-text { font-size: 20rpx; color: #999; }
.t-text.on { color: #4FAE84; font-weight: 600; }
.t-plus { width: 90rpx; height: 90rpx; border-radius: 50%; background: linear-gradient(135deg, #4FAE84, #7EC8A0); display: flex; align-items: center; justify-content: center; border: 5rpx solid #fff; box-shadow: 0 4rpx 16rpx rgba(79,174,132,0.3); margin-top: -16rpx; }
.t-plus-text { font-size: 48rpx; color: #fff; font-weight: bold; line-height: 1; }
.t-icon-wrap { position: relative; }
.t-badge { position: absolute; top: -8rpx; right: -16rpx; min-width: 32rpx; height: 32rpx; background: #FF4757; color: #fff; border-radius: 16rpx; font-size: 18rpx; text-align: center; line-height: 32rpx; padding: 0 6rpx; }
</style>
