<template>
  <view class="page">
    <scroll-view scroll-x class="rank-tabs">
      <view class="rt-item" :class="{ active: currentTab === 'hot' }" @tap="switchTab('hot')">🔥 热门</view>
      <view class="rt-item" :class="{ active: currentTab === 'weekly' }" @tap="switchTab('weekly')">📅 本周</view>
      <view class="rt-item" :class="{ active: currentTab === 'monthly' }" @tap="switchTab('monthly')">📆 本月</view>
      <view v-for="cat in categories" :key="cat.id" class="rt-item" :class="{ active: currentTab === 'cat_' + cat.id }" @tap="switchTab('cat_' + cat.id)">{{ cat.name }}</view>
    </scroll-view>

    <view v-if="list.length === 0" class="empty"><text>暂无排行数据</text></view>
    <view v-for="(item, i) in list" :key="i" class="rank-item" @tap="goDetail(item.id)">
      <view class="r-badge" :class="{ gold: i === 0, silver: i === 1, bronze: i === 2 }">{{ i + 1 }}</view>
      <view class="r-info">
        <view class="r-title-row">
          <text class="r-title">{{ item.title }}</text>
        </view>
        <view class="r-meta-row">
          <text class="r-meta">{{ item.nickname }}</text>
          <text class="r-meta-dot">·</text>
          <text class="r-meta">{{ item.categoryName }}</text>
        </view>
      </view>
      <view class="r-stats">
        <text class="r-stat">👍 {{ item.likeCount }}</text>
        <text class="r-stat">💬 {{ item.commentCount }}</text>
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
import { ref, onMounted } from 'vue'
import { rankApi } from '../../api/rank'
import { categoryApi } from '../../api/category'

const currentTab = ref('hot')
const list = ref([])
const categories = ref([])
const tabIdx = ref(1)

onMounted(async () => {
  const catRes = await categoryApi.list()
  categories.value = catRes.data
  fetchRank()
})

async function fetchRank() {
  let res
  if (currentTab.value === 'hot') res = await rankApi.getHot(20)
  else if (currentTab.value === 'weekly') res = await rankApi.getWeekly(20)
  else if (currentTab.value === 'monthly') res = await rankApi.getMonthly(20)
  else {
    const catId = currentTab.value.replace('cat_', '')
    res = await rankApi.getByCategory(catId, 20)
  }
  list.value = res.data || []
}

function switchTab(tab) { currentTab.value = tab; fetchRank() }
function goDetail(id) { uni.navigateTo({ url: `/pages/post/detail/detail?id=${id}` }) }
function goTab(i, url) {
  if (i === tabIdx.value) return
  const token = uni.getStorageSync('token')
  if ([2,3,4].includes(i) && !token) { uni.showToast({ title: '请先登录', icon: 'none' }); uni.navigateTo({ url: '/pages/login/login' }); return }
  tabIdx.value = i; uni.reLaunch({ url })
}
</script>

<style scoped>
.page { padding-bottom: 120rpx; }
.rank-tabs { white-space: nowrap; padding: 16rpx 24rpx; background: #fff; margin-bottom: 16rpx; }
.rt-item { display: inline-block; padding: 12rpx 28rpx; margin-right: 12rpx; border-radius: 32rpx; font-size: 26rpx; color: #666; background: #E2F0E7; }
.rt-item.active { background: #4FAE84; color: #fff; }
.rank-item { display: flex; align-items: center; gap: 20rpx; background: #fff; margin: 0 24rpx 12rpx; border-radius: 16rpx; padding: 24rpx 20rpx; }
.r-badge { width: 64rpx; height: 64rpx; border-radius: 50%; background: #e0e0e0; display: flex; align-items: center; justify-content: center; font-weight: bold; font-size: 28rpx; flex-shrink: 0; color: #666; }
.r-badge.gold { background: #ffd700; color: #fff; }
.r-badge.silver { background: #c0c0c0; color: #fff; }
.r-badge.bronze { background: #cd7f32; color: #fff; }
.r-info { flex: 1; min-width: 0; }
.r-title-row { display: flex; align-items: center; }
.r-title { font-size: 28rpx; font-weight: 600; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-bottom: 4rpx; }
.r-meta-row { display: flex; align-items: center; gap: 6rpx; }
.r-meta { font-size: 22rpx; color: #B2BEC3; }
.r-meta-dot { font-size: 22rpx; color: #B2BEC3; }
.r-stats { display: flex; flex-direction: column; gap: 4rpx; flex-shrink: 0; align-items: flex-end; }
.r-stat { font-size: 22rpx; color: #666; }
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
