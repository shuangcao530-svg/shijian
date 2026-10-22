<template>
  <view class="page">
    <view class="header-bar">
      <text class="title">通知中心</text>
      <text v-if="unreadCount > 0" class="mark-all-btn" @tap="markAllRead">全部已读</text>
    </view>

    <view v-if="notifications.length === 0 && !loading" class="empty">
      <text class="empty-icon">🔔</text>
      <text class="empty-text">暂无通知</text>
    </view>

    <view v-for="n in notifications" :key="n.id" class="notif-item" :class="{ unread: n.isRead === 0 }" @tap="handleTap(n)">
      <image class="n-avatar" :src="'/static/default-avatar.svg'" mode="aspectFill" />
      <view class="n-body">
        <text class="n-content">{{ n.content }}</text>
        <text class="n-time">{{ formatTime(n.createTime) }}</text>
      </view>
      <view v-if="n.isRead === 0" class="n-dot"></view>
    </view>

    <view v-if="hasMore" class="load-more" @tap="loadMore">
      <text class="load-text">{{ loading ? '加载中...' : '加载更多' }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { notificationApi } from '../../api/notification'

const notifications = ref([])
const unreadCount = ref(0)
const loading = ref(false)
const page = ref(1)
const hasMore = ref(true)

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t.replace(/-/g, '/'))
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return t.substring(0, 10)
}

async function fetchData(reset = false) {
  if (loading.value) return
  if (reset) { page.value = 1; notifications.value = [] }
  loading.value = true
  try {
    const res = await notificationApi.list(page.value, 20)
    if (reset) notifications.value = res.data.records
    else notifications.value = [...notifications.value, ...res.data.records]
    hasMore.value = res.data.records.length >= 20
    page.value++
  } finally { loading.value = false }
}

async function fetchUnread() {
  try {
    const res = await notificationApi.getUnreadCount()
    unreadCount.value = res.data
  } catch (e) { /* ignore */ }
}

async function markAllRead() {
  await notificationApi.markAllRead()
  notifications.value.forEach(n => n.isRead = 1)
  unreadCount.value = 0
  uni.showToast({ title: '已全部标为已读', icon: 'none' })
}

function handleTap(n) {
  if (n.isRead === 0) {
    notificationApi.markRead(n.id)
    n.isRead = 1
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }
  // Navigate to target
  if (n.targetType === 'POST' && n.targetId) {
    uni.navigateTo({ url: `/pages/post/detail/detail?id=${n.targetId}` })
  } else if (n.targetType === 'FRIEND_REQUEST') {
    uni.navigateTo({ url: '/pages/friend/friend' })
  }
}

function loadMore() { fetchData() }

onMounted(() => {
  fetchData(true)
  fetchUnread()
})
</script>

<style scoped>
.page { padding: 24rpx; min-height: 100vh; background: #EEF8F0; }
.header-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20rpx; padding: 0 8rpx; }
.title { font-size: 36rpx; font-weight: 700; color: #173B2F; }
.mark-all-btn { font-size: 26rpx; color: #4FAE84; }

.notif-item { display: flex; align-items: center; gap: 16rpx; background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 10rpx; position: relative; }
.notif-item.unread { background: #F0FAF4; border-left: 6rpx solid #4FAE84; }
.n-avatar { width: 72rpx; height: 72rpx; border-radius: 50%; flex-shrink: 0; background: #E8F5EC; }
.n-body { flex: 1; min-width: 0; }
.n-content { font-size: 27rpx; color: #2D4A3B; display: block; line-height: 1.5; }
.n-time { font-size: 22rpx; color: #8DA49A; margin-top: 6rpx; display: block; }
.n-dot { width: 14rpx; height: 14rpx; background: #FF4757; border-radius: 50%; flex-shrink: 0; }

.empty { text-align: center; padding: 200rpx 0; }
.empty-icon { font-size: 80rpx; display: block; margin-bottom: 20rpx; }
.empty-text { font-size: 28rpx; color: #8DA49A; }

.load-more { text-align: center; padding: 28rpx; }
.load-text { font-size: 26rpx; color: #8DA49A; }
</style>
