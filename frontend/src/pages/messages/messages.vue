<template>
  <view class="page">
    <!-- Friend Requests -->
    <view v-if="requests.length" class="section">
      <text class="section-title">好友请求</text>
      <view v-for="r in requests" :key="r.id" class="req-item">
        <image class="r-avatar" :src="r.avatar || '/static/default-avatar.svg'" mode="aspectFill" />
        <view class="r-info">
          <text class="r-name">{{ r.nickname || r.username }}</text>
          <text class="r-desc">请求添加你为好友</text>
        </view>
        <view class="r-btns">
          <button size="mini" class="btn-accept" @tap="handleReq(r.requestId, 'ACCEPT')">接受</button>
          <button size="mini" class="btn-reject" @tap="handleReq(r.requestId, 'REJECT')">拒绝</button>
        </view>
      </view>
    </view>

    <!-- Notification Entry -->
    <view class="section">
      <view class="conv-item" @tap="goNotifications">
        <text class="notif-icon">🔔</text>
        <view class="c-info">
          <text class="c-name">通知中心</text>
          <text class="c-last">查看点赞、评论、好友请求等通知</text>
        </view>
        <view v-if="notifUnread > 0" class="notif-badge">{{ notifUnread > 99 ? '99+' : notifUnread }}</view>
        <text class="c-arrow">›</text>
      </view>
    </view>

    <!-- Conversations -->
    <view class="section">
      <text class="section-title">私信</text>
      <view v-if="convs.length === 0" class="empty"><text>暂无消息</text></view>
      <view v-for="c in convs" :key="c.userId" class="conv-item" @tap="goChat(c)">
        <view class="c-avatar-wrap">
          <image class="c-avatar" :src="c.avatar || '/static/default-avatar.svg'" mode="aspectFill" />
          <view v-if="c.unread > 0" class="c-badge">{{ c.unread }}</view>
        </view>
        <view class="c-info">
          <text class="c-name">{{ c.nickname }}</text>
          <text class="c-last">{{ c.isMe ? '我: ' : '' }}{{ c.lastMsg }}</text>
        </view>
        <text class="c-time">{{ c.time }}</text>
      </view>
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
import { messageApi } from '../../api/message'
import { friendApi } from '../../api/friend'
import { notificationApi } from '../../api/notification'

const tabIdx = ref(3)
const convs = ref([])
const requests = ref([])
const unread = ref(0)
const notifUnread = ref(0)

function goTab(i, url) {
  if (i === tabIdx.value) return
  const token = uni.getStorageSync('token')
  if ([2,3,4].includes(i) && !token) { uni.showToast({ title: '请先登录', icon: 'none' }); uni.navigateTo({ url: '/pages/login/login' }); return }
  tabIdx.value = i; uni.reLaunch({ url })
}
function goChat(c) {
  uni.navigateTo({ url: `/pages/messages/chat?userId=${c.userId}&avatar=${encodeURIComponent(c.avatar || '')}` })
}
function goNotifications() {
  uni.navigateTo({ url: '/pages/messages/notifications' })
}
async function handleReq(id, action) {
  await friendApi.handleRequest(id, action)
  uni.showToast({ title: action === 'ACCEPT' ? '已接受' : '已拒绝' })
  loadData()
}
async function loadData() {
  try {
    const [cRes, rRes] = await Promise.allSettled([
      messageApi.getConversations(),
      friendApi.getRequests()
    ])
    if (cRes.status === 'fulfilled') convs.value = cRes.value.data || []
    if (rRes.status === 'fulfilled') requests.value = rRes.value.data || []
  } catch (e) {}
}
onMounted(() => {
  loadData()
  messageApi.getUnreadCount().then(r => { unread.value = r.data || 0 }).catch(() => {})
  notificationApi.getUnreadCount().then(r => { notifUnread.value = r.data || 0 }).catch(() => {})
})
</script>

<style scoped>
.page { padding: 20rpx 24rpx 140rpx; min-height: 100vh; background: #EEF8F0; }
.section { background: #fff; border-radius: 16rpx; padding: 20rpx; margin-bottom: 16rpx; }
.section-title { font-size: 30rpx; font-weight: bold; display: block; margin-bottom: 16rpx; }
.req-item { display: flex; align-items: center; gap: 16rpx; padding: 16rpx 0; border-bottom: 1rpx solid #f5f5f5; }
.req-item:last-child { border-bottom: none; }
.r-avatar { width: 80rpx; height: 80rpx; border-radius: 50%; }
.r-info { flex: 1; }
.r-name { font-size: 28rpx; font-weight: 600; display: block; }
.r-desc { font-size: 24rpx; color: #999; }
.r-btns { display: flex; gap: 8rpx; }
.btn-accept { background: #4FAE84; color: #fff; border: none; }
.btn-reject { background: #eee; color: #666; border: none; }
.conv-item { display: flex; align-items: center; gap: 16rpx; padding: 16rpx 0; border-bottom: 1rpx solid #f5f5f5; }
.conv-item:last-child { border-bottom: none; }
.c-avatar-wrap { position: relative; }
.c-avatar { width: 90rpx; height: 90rpx; border-radius: 50%; }
.c-badge { position: absolute; top: -6rpx; right: -6rpx; min-width: 36rpx; height: 36rpx; background: #FF4757; color: #fff; border-radius: 18rpx; font-size: 20rpx; text-align: center; line-height: 36rpx; padding: 0 6rpx; }
.c-info { flex: 1; min-width: 0; }
.c-name { font-size: 28rpx; font-weight: 600; display: block; }
.c-last { font-size: 24rpx; color: #999; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; display: block; }
.c-time { font-size: 22rpx; color: #B2BEC3; flex-shrink: 0; }
.empty { text-align: center; padding: 40rpx; font-size: 26rpx; color: #999; }
.notif-icon { font-size: 44rpx; flex-shrink: 0; }
.notif-badge { min-width: 36rpx; height: 36rpx; background: #FF4757; color: #fff; border-radius: 18rpx; font-size: 20rpx; text-align: center; line-height: 36rpx; padding: 0 8rpx; flex-shrink: 0; }
.c-arrow { font-size: 32rpx; color: #ccc; flex-shrink: 0; }

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
