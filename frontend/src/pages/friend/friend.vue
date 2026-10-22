<template>
  <view class="page">
    <view class="tabs">
      <view class="tab-item" :class="{ active: tab === 0 }" @tap="tab = 0">好友</view>
      <view class="tab-item" :class="{ active: tab === 1 }" @tap="tab = 1">请求</view>
      <view class="tab-item" :class="{ active: tab === 2 }" @tap="tab = 2">黑名单</view>
    </view>

    <!-- Search -->
    <view v-if="tab === 0" class="search-row">
      <input class="search-input" v-model="keyword" placeholder="搜索用户..." @confirm="searchUsers" />
      <view v-if="searchResults.length" class="search-results">
        <view v-for="u in searchResults" :key="u.id" class="s-item">
          <image class="s-avatar" :src="u.avatar || '/static/default-avatar.svg'" mode="aspectFill" />
          <view class="s-info"><text class="s-name">{{ u.nickname || u.username }}</text></view>
          <button size="mini" class="s-btn" @tap="sendRequest(u.id)">添加</button>
        </view>
      </view>
    </view>

    <!-- Friends -->
    <view v-if="tab === 0">
      <view v-if="friends.length === 0" class="empty"><text>暂无好友</text></view>
      <view v-for="f in friends" :key="f.id" class="f-item">
        <image class="f-avatar" :src="f.avatar || '/static/default-avatar.svg'" mode="aspectFill" @tap="goUser(f.id)" />
        <view class="f-info" @tap="goUser(f.id)"><text class="f-name">{{ f.nickname || f.username }}</text></view>
        <button size="mini" class="f-chat" @tap="goChat(f)">发消息</button>
        <button size="mini" class="f-del" @tap="delFriend(f.id)">删除</button>
      </view>
    </view>

    <!-- Requests -->
    <view v-if="tab === 1">
      <view v-if="requests.length === 0" class="empty"><text>暂无好友请求</text></view>
      <view v-for="r in requests" :key="r.id" class="f-item">
        <image class="f-avatar" :src="r.avatar || '/static/default-avatar.svg'" mode="aspectFill" />
        <view class="f-info"><text class="f-name">{{ r.nickname || r.username }}</text></view>
        <button size="mini" class="f-accept" @tap="handleReq(r.requestId, 'ACCEPT')">接受</button>
        <button size="mini" class="f-del" @tap="handleReq(r.requestId, 'REJECT')">拒绝</button>
      </view>
    </view>

    <!-- Blacklist -->
    <view v-if="tab === 2">
      <view v-if="blacklist.length === 0" class="empty"><text>黑名单为空</text></view>
      <view v-for="b in blacklist" :key="b.id" class="f-item">
        <image class="f-avatar" :src="b.avatar || '/static/default-avatar.svg'" mode="aspectFill" />
        <view class="f-info"><text class="f-name">{{ b.nickname || b.username }}</text></view>
        <button size="mini" class="f-accept" @tap="removeBlack(b.id)">移出</button>
      </view>
    </view>
    <view class="tab-bar">
      <view class="t-item" @tap="goTab(0, '/pages/index/index')"><text class="t-icon">🏠</text><text class="t-text" :class="{ on: tabIdx===0 }">首页</text></view>
      <view class="t-item" @tap="goTab(1, '/pages/moments/moments')"><text class="t-icon">🌿</text><text class="t-text" :class="{ on: tabIdx===1 }">好友圈</text></view>
      <view class="t-item" @tap="goTab(2, '/pages/post/create/create')"><view class="t-plus"><text class="t-plus-text">＋</text></view></view>
      <view class="t-item" @tap="goTab(3, '/pages/messages/messages')"><text class="t-icon">💬</text><text class="t-text" :class="{ on: tabIdx===3 }">消息</text></view>
      <view class="t-item" @tap="goTab(4, '/pages/user/profile/profile')"><text class="t-icon">👤</text><text class="t-text" :class="{ on: tabIdx===4 }">我的</text></view>
    </view>
  </view>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { friendApi } from '../../api/friend'
import { userApi } from '../../api/user'

const tab = ref(0)
const friends = ref([])
const requests = ref([])
const blacklist = ref([])
const keyword = ref('')
const searchResults = ref([])
const tabIdx = ref(3)

onMounted(() => {
  loadTabData()
})

watch(tab, () => loadTabData())

async function loadTabData() {
  if (tab.value === 0) { const r = await friendApi.getFriends(); friends.value = r.data.records || [] }
  else if (tab.value === 1) { const r = await friendApi.getRequests(); requests.value = r.data || [] }
  else { const r = await friendApi.getBlacklist(); blacklist.value = r.data.records || [] }
}

async function searchUsers() {
  if (!keyword.value.trim()) { searchResults.value = []; return }
  const r = await userApi.searchUsers(keyword.value)
  searchResults.value = r.data.records || []
}

async function sendRequest(id) {
  await friendApi.sendRequest(id, '')
  uni.showToast({ title: '已发送' })
  keyword.value = ''
  searchResults.value = []
}

async function delFriend(id) {
  const cf = await new Promise(r => uni.showModal({ title: '确认删除好友？', success: e => r(e.confirm) }))
  if (!cf) return
  await friendApi.deleteFriend(id)
  uni.showToast({ title: '已删除' })
  loadTabData()
}

async function handleReq(id, action) {
  await friendApi.handleRequest(id, action)
  uni.showToast({ title: action === 'ACCEPT' ? '已接受' : '已拒绝' })
  loadTabData()
}

async function removeBlack(id) {
  await friendApi.removeFromBlacklist(id)
  uni.showToast({ title: '已移除' })
  loadTabData()
}

function goUser(id) { uni.navigateTo({ url: `/pages/user/info/info?id=${id}` }) }
function goChat(f) { uni.navigateTo({ url: `/pages/messages/chat?userId=${f.id}&avatar=${encodeURIComponent(f.avatar || '')}` }) }
function goTab(i, url) {
  if (i === tabIdx.value) return
  const token = uni.getStorageSync('token')
  if ([2,3,4].includes(i) && !token) { uni.showToast({ title: '请先登录', icon: 'none' }); uni.navigateTo({ url: '/pages/login/login' }); return }
  tabIdx.value = i; uni.reLaunch({ url })
}
</script>

<style scoped>
.page { padding: 24rpx; padding-bottom: 120rpx; }
.tabs { display: flex; background: #fff; border-radius: 12rpx; overflow: hidden; margin-bottom: 16rpx; }
.tab-item { flex: 1; text-align: center; padding: 20rpx 0; font-size: 28rpx; color: #666; }
.tab-item.active { color: #4FAE84; font-weight: bold; border-bottom: 3rpx solid #4FAE84; }
.search-row { margin-bottom: 16rpx; }
.search-input { height: 72rpx; background: #fff; border-radius: 36rpx; padding: 0 24rpx; font-size: 26rpx; }
.search-results { background: #fff; border-radius: 12rpx; margin-top: 8rpx; }
.s-item { display: flex; align-items: center; gap: 16rpx; padding: 16rpx; border-bottom: 1rpx solid #f5f5f5; }
.s-avatar { width: 72rpx; height: 72rpx; border-radius: 50%; }
.s-info { flex: 1; }
.s-name { font-size: 28rpx; font-weight: 600; }
.s-btn { background: #4FAE84; color: #fff; border: none; }
.f-item { display: flex; align-items: center; gap: 16rpx; background: #fff; padding: 20rpx; border-radius: 12rpx; margin-bottom: 12rpx; }
.f-avatar { width: 80rpx; height: 80rpx; border-radius: 50%; }
.f-info { flex: 1; }
.f-name { font-size: 28rpx; font-weight: 600; }
.f-del { background: #D96666; color: #fff; border: none; font-size: 22rpx; }
.f-chat { background: #4FAE84; color: #fff; border: none; font-size: 22rpx; }
.f-accept { background: #67c23a; color: #fff; border: none; font-size: 22rpx; }
.empty { text-align: center; padding: 80rpx 0; font-size: 28rpx; color: #999; }
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
